package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.DriveOperation;

public class ln180648_MyDriveOperation implements DriveOperation {

    private Connection conn;

    ln180648_MyDriveOperation(Connection conn) {
        this.conn = conn;
    }

    @Override
    public boolean planingDrive(String string) {
//        System.out.println("Planiraj voznju " + string);
        String query = "select KorisnickoIme from Kurir where KorisnickoIme = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji kurir sa datim korImenom
                int idGrad = dohvatanjeGradaKurira(string);   //Grad u kojem zivi kurir
                int idMagacin = dohvatanjeMagacina(idGrad);   //Magacin u gradu u kojem zivi kurir
                if (idMagacin == -1) {    //Ne postoji magacin u gradu
                    return false;
                }

                int trenutnaLokacija = faza1(string, idMagacin, idGrad);
                //Preuzimanje slobodnog vozila iz magacina + 
                //planiranje preuzimanja paketa iz grada u kome se nalazi

                if (trenutnaLokacija == -1) {  //Ako ne moze da se izvrsi faza1
                    return false;
                }

                int brojPaketaZaIsporuku = dohvatiBrojPaketaZaIsporuku(string);

                faza2(string, brojPaketaZaIsporuku, trenutnaLokacija);
//                System.out.println("FAZA2 gotova");
                int idVoznja = dohvatiIdVoznje(string);
                resetujRBSljedecaStanica(idVoznja);

                return true;

            } else {  //Ne postoji kurir sa datim korImenom
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public int nextStop(String string) {
//        System.out.println("rs.etf.sab.student.ln180648_MyDriveOperation.nextStop() " + string);
        String query = "select RegistracioniBroj, Lokacija from Vozi where Vozac = ?";
        String query2 = "select IdPak, IdAdr, IdGrad from PlanPreuzimanja where RedniBroj = ? and IdVoznja = ?";
        String query3 = "select IdPak, IdAdr from PlanIsporuke where RedniBroj = ? and IdVoznja = ?";
        String query4 = "select IdAdr from Magacin where IdMag = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3);
                PreparedStatement ps4 = conn.prepareStatement(query4)) {
            int idVoznja = dohvatiIdVoznje(string);
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String registracioniBroj = rs.getString(1);
            int lokacijaVozaca = rs.getInt(2);

            ps2.setInt(1, dohvatiRBSljedecaStanica(idVoznja));
            ps2.setInt(2, idVoznja);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {  //Sa zadate lokacije vozac samo preuzima paket/pakete
                int idPak = rs2.getInt(1);
                int idAdr = rs2.getInt(2);
                int idGrad = rs2.getInt(3);
//                System.out.println("PREUZIMA " + idPak + " iz grada " + idGrad);

                azurirajPredjeniPut(lokacijaVozaca, idAdr, idVoznja);

                preuzimaPaket(idPak, idAdr, registracioniBroj, string);
                while (rs2.next()) {    //Preuzima i ostale pakete sa iste adrese (iz istog magacina)
                    idPak = rs2.getInt(1);
                    idAdr = rs2.getInt(2);
                    idGrad = rs2.getInt(3);
//                    System.out.println("PREUZIMA " + idPak + " iz grada " + idGrad);
                    preuzimaPaket(idPak, idAdr, registracioniBroj, string);
                }
                inkrementirajRBSljedecaStanica(idVoznja);
                return -2;
            } else { //Sa zadate lokacije vozac ne preuzima paket
                ps3.setInt(1, dohvatiRBSljedecaStanica(idVoznja));
                ps3.setInt(2, idVoznja);
                ResultSet rs3 = ps3.executeQuery();
                if (rs3.next()) { //Izvrsena isporuka paketa na zadatu lokaciju
                    int idPak = rs3.getInt(1);
                    int idAdr = rs3.getInt(2);
                    int idGrad = dohvatIdGrada(idAdr);
//                    System.out.println("ISPORUCUJE " + idPak + " u grad " + idGrad);

                    azurirajPredjeniPut(lokacijaVozaca, idAdr, idVoznja);

                    return isporucujePaket(idPak, idAdr, registracioniBroj, string);
                } else { //Zavrseno isporucivanje i vracanje u startni magacin
//                    System.out.println("ZAVRSAVA");
                    int idGrad = dohvatanjeGradaKurira(string);
                    int idMag = dohvatanjeMagacina(idGrad);
                    ps4.setInt(1, idMag);
                    ResultSet rs4 = ps4.executeQuery();
                    rs4.next();
                    int idAdr = rs4.getInt(1);

                    azurirajPredjeniPut(lokacijaVozaca, idAdr, idVoznja);

                    return zavrsavaIsporucivanje(string, registracioniBroj);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return -3;
        }

    }

    @Override
    public List<Integer> getPackagesInVehicle(String string) {
//        System.out.println("rs.etf.sab.student.ln180648_MyDriveOperation.getPackagesInVehicle()");
        String query = "select RegistracioniBroj from Vozi where Vozac = ?";
        String query2 = "select IdPak from UVozilu where RegistracioniBroj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String registracioniBroj = rs.getString(1);
            List<Integer> listaPaketaUVozilu = new ArrayList<>();
            ps2.setString(1, registracioniBroj);
            ResultSet rs2 = ps2.executeQuery();
//            System.out.println("U vozilu su paketi: ");
            while (rs2.next()) {
                listaPaketaUVozilu.add(rs2.getInt(1));
//                System.out.println("Paket " + rs2.getInt(1));
            }
            return listaPaketaUVozilu;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public int dohvatanjeGradaKurira(String string) {
        String query = "select IdAdr from Korisnik where KorisnickoIme = ?";
        String query2 = "select IdGrad from Adresa where IdAdr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int idAdr = rs.getInt(1);
            ps2.setInt(1, idAdr);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            return rs2.getInt(1); //Grad u kojem zivi kurir

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

    }

    public int dohvatanjeMagacina(int idGrad) {
        String query = "select IdAdr from Adresa where IdGrad = ?";
        String query2 = "select IdMag from Magacin where IdAdr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, idGrad);
            ResultSet rs = ps.executeQuery();
            List<Integer> listaAdresaUGradu = new ArrayList<>();
            int idMagacin = -1;
            int adresaMagacina = -1;
            while (rs.next()) {  //Prolazak kroz sve adrese grada
                listaAdresaUGradu.add(rs.getInt(1));
                ps2.setInt(1, rs.getInt(1));
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) { //Ako postoji magacin na adresi
                    adresaMagacina = rs.getInt(1);
                    idMagacin = rs2.getInt(1);
                    break;
                }
            }

            return idMagacin;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public int dohvatanjeAdreseMagacina(int idMag) {
        String query = "select IdAdr from Magacin where IdMag = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idMag);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public String preuzimanjeSlobodnogVozila(int idMagacin, String string) {
        String query = "select RegistracioniBroj from Vozilo where IdMag = ?";
        String query2 = "select IdAdr from Magacin where IdMag = ?";
        String query3 = "insert into Voznja(RegistracioniBroj, Vozac) values(?, ?)";
        String query4 = "update Kurir set Status = 1 where KorisnickoIme = ?";
        String query5 = "insert into Vozi(RegistracioniBroj, Vozac, Lokacija, IdVoznja) values(?, ?, ?, ?)";
        String query6 = "update Vozilo set idMag = null where RegistracioniBroj = ?";  //OVO JE DODATO !!!
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement ps4 = conn.prepareStatement(query4);
                PreparedStatement ps5 = conn.prepareStatement(query5);
                PreparedStatement ps6 = conn.prepareStatement(query6)) {
            String registracioniBroj = null;
            ps.setInt(1, idMagacin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { //Ako postoji slobodno vozilo parkirano u magacinu
                registracioniBroj = rs.getString(1);
                ps2.setInt(1, idMagacin);
                ResultSet rs2 = ps2.executeQuery();
                rs2.next();
                int adresaMagacina = rs2.getInt(1);
                ps3.setString(1, registracioniBroj);
                ps3.setString(2, string);
                ps3.executeUpdate();
                ResultSet rs3 = ps3.getGeneratedKeys();
                rs3.next();
                int idVoznja = rs3.getInt(1);
                ps4.setString(1, string);
                ps4.executeUpdate();
                ps5.setString(1, registracioniBroj);
                ps5.setString(2, string);
                ps5.setInt(3, adresaMagacina);
                ps5.setInt(4, idVoznja);
                ps5.executeUpdate();
                ps6.setString(1, registracioniBroj);
                ps6.executeUpdate();
                return registracioniBroj;
            } else { //Ne postoji slobodno vozilo
                return null;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public int planiranjePreuzimanjaPaketaIzGrada(String registracioniBroj, String string, int idGrad, int stop) {
        String query = "select IdAdr from Adresa where IdGrad = ?";
        String query2 = "select IdMag from Magacin where IdAdr = ?";
        String query3 = "select IdPak from Paket where Status = 1 order by VrijemePrihvatanja asc";
        String query4 = "select Tezina, IdPak from Paket where IdPak = ? and IdAdrSa = ?";
        String query5 = "insert into PlanPreuzimanja(IdVoznja, IdPak, IdGrad, IdAdr, Stop, RedniBroj) values(?, ?, ?, ?, ?, ?)";
        String query6 = "select IdPak, Tezina, VrijemePrihvatanja from Paket where Status = 2 and Lokacija = ? order by VrijemePrihvatanja asc";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                Statement s3 = conn.createStatement();
                PreparedStatement ps4 = conn.prepareStatement(query4);
                PreparedStatement ps5 = conn.prepareStatement(query5);
                PreparedStatement ps6 = conn.prepareStatement(query6)) {
            int trenutnaLokacija = -1;
            int idVoznja = dohvatiIdVoznje(string); //OVO JE DODATO
            List<Integer> listaAdresaUGradu = new ArrayList<>();
            int adresaMagacina = -1;
            BigDecimal nosivost = dohvatiNosivostVozila(registracioniBroj);
//            System.out.println("NOSIVOST " + nosivost);
            BigDecimal ukupnaTezina = dohvatiUkTezinuPaketaUVozilu(idVoznja);

            ps.setInt(1, idGrad);
            ResultSet rs = ps.executeQuery();
//            System.out.println("GRAD " + idGrad);
            while (rs.next()) {  //Prolazak kroz sve adrese grada
                listaAdresaUGradu.add(rs.getInt(1));
                ps2.setInt(1, rs.getInt(1));
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) { //Ako postoji magacin na adresi
                    adresaMagacina = rs.getInt(1);
                }
            }
            
            ResultSet rs3 = s3.executeQuery(query3);
            while (rs3.next()) {  //Prolazak kroz sve prihvacene pakete po redoslijedu prihvatanja
                int idPaket = rs3.getInt(1);
//                System.out.println(" prihvacenPaket(ne nalazi se nuzno u trenutnom gradu): " + idPaket);
                for (int i = 0; i < listaAdresaUGradu.size(); i++) {
                    ps4.setInt(1, idPaket);
                    ps4.setInt(2, listaAdresaUGradu.get(i));
                    ResultSet rs4 = ps4.executeQuery();
                    while (rs4.next()) { //Postoji paket/paketi u gradu da se pokupi sa neke adrese
//                        System.out.println("UKUPNA TEZINA PRIJE: " + ukupnaTezina + " paketId: " + rs4.getInt(2));
                        BigDecimal tezina = rs4.getBigDecimal(1);
                        if (ukupnaTezina.add(tezina).compareTo(nosivost) <= 0) {
                            ukupnaTezina = ukupnaTezina.add(tezina);
                            azurirajUkTezinuPaketaUVozilu(idVoznja, ukupnaTezina);
//                            System.out.println("UKUPNA TEZINA POSLIJE: " + ukupnaTezina + " paketId: " + rs4.getInt(2));
                            trenutnaLokacija = listaAdresaUGradu.get(i);
                            ps5.setInt(1, idVoznja);
                            ps5.setInt(2, idPaket);
                            ps5.setInt(3, idGrad);
                            ps5.setInt(4, listaAdresaUGradu.get(i));
                            ps5.setInt(5, stop);
                            ps5.setInt(6, dohvatiRBSljedecaStanica(idVoznja));
                            inkrementirajRBSljedecaStanica(idVoznja);
                            ps5.executeUpdate();
                        }
                    }
                }
            }

            int brojacPokupljenihIzMagacina = 0;
            ps6.setInt(1, adresaMagacina);
            ResultSet rs6 = ps6.executeQuery();
            while (rs6.next()) { //Prolazak kroz sve pakete iz magacina po redoslijedu prihvatanja
                int idPaket = rs6.getInt(1);
//                System.out.println("Paket iz magacina " + idPaket + " vrijeme prihvatanja: " + rs6.getTimestamp(3));
//                System.out.println("UKUPNA TEZINA2 PRIJE: " + ukupnaTezina + " paketId " + idPaket);
                BigDecimal tezina = rs6.getBigDecimal(2);
                if (ukupnaTezina.add(tezina).compareTo(nosivost) <= 0) {
                    ukupnaTezina = ukupnaTezina.add(tezina);
                    azurirajUkTezinuPaketaUVozilu(idVoznja, ukupnaTezina);
//                    System.out.println("UKUPNA TEZINA2 POSLIJE: " + ukupnaTezina + " paketId: " + idPaket);
                    trenutnaLokacija = adresaMagacina;
                    ps5.setInt(1, idVoznja);
                    ps5.setInt(2, idPaket);
                    ps5.setInt(3, idGrad);
                    ps5.setInt(4, adresaMagacina);
                    ps5.setInt(5, stop);
                    ps5.setInt(6, dohvatiRBSljedecaStanica(idVoznja));
                    brojacPokupljenihIzMagacina++;
                    ps5.executeUpdate();
                }
            }
            if (brojacPokupljenihIzMagacina > 0) {
                inkrementirajRBSljedecaStanica(idVoznja);
            }
            return trenutnaLokacija;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

    }

    public int faza1(String string, int idMagacin, int idGrad) {
//        System.out.println("FAZA1 " + string + " magacin " + idMagacin + " grad " + idGrad);
        String registracioniBroj = preuzimanjeSlobodnogVozila(idMagacin, string);   //Preuzeto vozilo iz magacina
        if (registracioniBroj == null) {  //Nema slobodnog vozila u magacinu
            return -1;
        }

        return planiranjePreuzimanjaPaketaIzGrada(registracioniBroj, string, idGrad, 1); //Planiranje preuzimanja paketa za pocetni grad

    }

    public void faza2(String string, int brojPaketaZaIsporuku, int pocetnaLokacija) {
        int trenutnaLokacija = pocetnaLokacija;
        for (int i = 1; i <= brojPaketaZaIsporuku; i++) {
            String query = "select XKoordinata, YKoordinata from Adresa where IdAdr = ?";
            String query2 = "select IdPak from PlanPreuzimanja where IdVoznja = ? and Stop = 1 and Status = 0";
            String query3 = "select IdAdrNa from Paket where IdPak = ?";

            try (PreparedStatement ps = conn.prepareStatement(query);
                    PreparedStatement ps2 = conn.prepareStatement(query2);
                    PreparedStatement ps3 = conn.prepareStatement(query3)) {
                int idVoznja = dohvatiIdVoznje(string);
                double min = Double.MAX_VALUE;
                int adresaNajblizeLokacije = -1;

                ps.setInt(1, trenutnaLokacija);
                ResultSet rs = ps.executeQuery();
                rs.next();
                int xVozaca = rs.getInt(1);
                int yVozaca = rs.getInt(2);

                int idPaketaNajblizaAdresa = 0;
                ps2.setInt(1, idVoznja);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {  //Prolazak kroz sve pakete koje treba isporuciti, a nisu jos isporuceni
                    ps3.setInt(1, rs2.getInt(1));
                    ResultSet rs3 = ps3.executeQuery();
                    rs3.next();
                    int idAdresaNa = rs3.getInt(1);

                    ps.setInt(1, idAdresaNa);
                    ResultSet rs12 = ps.executeQuery();
                    rs12.next();
                    int xAdresaNa = rs12.getInt(1);
                    int yAdresaNa = rs12.getInt(2);
                    double euklidskaDistanca = Math.sqrt(Math.pow(xAdresaNa - xVozaca, 2) + Math.pow(yAdresaNa - yVozaca, 2));
                    if (euklidskaDistanca < min) {
                        min = euklidskaDistanca;
                        adresaNajblizeLokacije = idAdresaNa;
                        idPaketaNajblizaAdresa = rs2.getInt(1);
                    }
                }

                ubaciUPlanIsporuke(idVoznja, idPaketaNajblizaAdresa, adresaNajblizeLokacije);

                String registracioniBroj = dohvatiVoziloVozaca(string);

                int idGrad1 = dohvatIdGrada(trenutnaLokacija);
                int idGrad2 = dohvatIdGrada(adresaNajblizeLokacije);
                if (idGrad1 != idGrad2) {
                    //Ako se ide u neki drugi grad, 
                    //treba pogledati da li postoje paketi za preuzimanje iz tog grada
                    planiranjePreuzimanjaPaketaIzGrada(registracioniBroj, string, idGrad2, i + 1);
                }

                trenutnaLokacija = adresaNajblizeLokacije;

            } catch (SQLException ex) {
                Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public int dohvatiBrojPaketaZaIsporuku(String string) {
        String query = "select count(IdPak) from PlanPreuzimanja where IdVoznja = ? and Stop = 1";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            int idVoznja = dohvatiIdVoznje(string);
            ps.setInt(1, idVoznja);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public void preuzimaPaket(int idPak, int idAdr, String registracioniBroj, String string) {
        azurirajLokacijuVozaca(idAdr, string);

        azurirajLokacijuSvihPaketaUVozilu(registracioniBroj, idAdr);

        ubaciPaketUVozilo(idPak, idAdr, registracioniBroj);
    }

    public int isporucujePaket(int idPak, int idAdr, String registracioniBroj, String string) {
        int idVoznja = dohvatiIdVoznje(string);

        azurirajLokacijuVozaca(idAdr, string);

        azurirajLokacijuSvihPaketaUVozilu(registracioniBroj, idAdr);

        izbaciPaketIzVozila(idPak);

        azurirajBrojIsporucenihPaketaKurira(string);

        ubaciUIsporuceno(idVoznja, string, idPak);

        inkrementirajRBSljedecaStanica(idVoznja);
//        System.out.println("rs.etf.sab.student.ln180648_MyDriveOperation.isporucujePaket() " + (idPak));
        return idPak;
    }

    public int zavrsavaIsporucivanje(String string, String registracioniBroj) {
        int idGrad = dohvatanjeGradaKurira(string);
        int idMag = dohvatanjeMagacina(idGrad);
        int adresaMagacina = dohvatanjeAdreseMagacina(idMag);

        ubaciNeisporucenePaketeUMagacin(registracioniBroj, idMag, adresaMagacina);

        azurirajStatusIProfitKurira(registracioniBroj, string);

        izbaciSvePaketeIzVozila(registracioniBroj);

        parkirajVoziloUMagacin(string, idMag, registracioniBroj);

        return -1;

    }

    public BigDecimal izracunajProfit(String string, String registracioniBroj) {
        String query = "select sum(Cijena) from Isporuceno where IdVoznja = ?";
        String query2 = "select TipGoriva, Potrosnja from Vozilo where RegistracioniBroj = ?";
        String query3 = "select PredjeniPut from Voznja where IdVoznja = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3)) {
            BigDecimal profit;
            int idVoznja = dohvatiIdVoznje(string);

            ps.setInt(1, idVoznja);
            ResultSet rs = ps.executeQuery();
            rs.next();
            BigDecimal ukupnaCijena = rs.getBigDecimal(1);

            ps2.setString(1, registracioniBroj);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            int tipGoriva = rs2.getInt(1);
            BigDecimal potrosnja = rs2.getBigDecimal(2);
//            System.out.println("tipGoriva " + tipGoriva + " potrosnja " + potrosnja);

            ps3.setInt(1, idVoznja);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            BigDecimal predjeniPut = rs3.getBigDecimal(1);
            BigDecimal potrosio;
            BigDecimal trosakGorivo;
//            System.out.println("Predjeni put " + predjeniPut);
            if (tipGoriva == 0) {
                potrosio = potrosnja.multiply(predjeniPut);
//                System.out.println("Potrosio " + potrosio);
                trosakGorivo = potrosio.multiply(new BigDecimal(15));
            } else if (tipGoriva == 1) {
                potrosio = potrosnja.multiply(predjeniPut);
//                System.out.println("Potrosio " + potrosio);
                trosakGorivo = potrosio.multiply(new BigDecimal(32));
            } else {
                potrosio = potrosnja.multiply(predjeniPut);
//                System.out.println("Potrosio " + potrosio);
                trosakGorivo = potrosio.multiply(new BigDecimal(36));
            }
            profit = ukupnaCijena.subtract(trosakGorivo);
            return profit;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public BigDecimal izracunajEuklidskuDistancu(int lokacijaVozaca, int idAdr) {
        String query = "select XKoordinata, YKoordinata from Adresa where IdAdr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, lokacijaVozaca);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int xVozaca = rs.getInt(1);
            int yVozaca = rs.getInt(2);

            ps.setInt(1, idAdr);
            ResultSet rs12 = ps.executeQuery();
            rs12.next();
            int xAdresa = rs12.getInt(1);
            int yAdresa = rs12.getInt(2);

            BigDecimal euklidskaDistanca = new BigDecimal(Math.sqrt(Math.pow(xAdresa - xVozaca, 2) + Math.pow(yAdresa - yVozaca, 2)));
            return euklidskaDistanca;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public int dohvatiIdVoznje(String string) {
        String query = "select IdVoznja from Vozi where Vozac = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public int dohvatiRBSljedecaStanica(int idVoznja) {
        String query = "select RBSljedecaStanica from Voznja where IdVoznja = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idVoznja);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public void inkrementirajRBSljedecaStanica(int idVoznja) {
        String query = "update Voznja set RBSljedecaStanica = RBSljedecaStanica + 1 where IdVoznja = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idVoznja);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void resetujRBSljedecaStanica(int idVoznja) {
        String query = "update Voznja set RBSljedecaStanica = 1 where IdVoznja = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idVoznja);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void azurirajUkTezinuPaketaUVozilu(int idVoznja, BigDecimal ukTezina) {
        String query = "update Voznja set UkupnaTezinaUVozilu = ? where IdVoznja = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setBigDecimal(1, ukTezina);
            ps.setInt(2, idVoznja);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BigDecimal dohvatiUkTezinuPaketaUVozilu(int idVoznja) {
        String query = "select UkupnaTezinaUVozilu from Voznja where IdVoznja = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idVoznja);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getBigDecimal(1);
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public int dohvatIdGrada(int idAdr) {
        String query = "select IdGrad from Adresa where IdAdr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idAdr);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public BigDecimal dohvatiNosivostVozila(String registracioniBroj) {
        String query = "select Nosivost from Vozilo where RegistracioniBroj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, registracioniBroj);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getBigDecimal(1);
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public void ubaciUPlanIsporuke(int idVoznja, int idPaketaNajblizaAdresa, int adresaNajblizeLokacije) {
        String query = "insert into PlanIsporuke(IdVoznja, IdPak, RedniBroj, IdAdr) values(?, ?, ?, ?)";
        String query2 = "update PlanPreuzimanja set Status = 1 where IdVoznja = ? and IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, idVoznja);
            ps.setInt(2, idPaketaNajblizaAdresa);
            ps.setInt(3, dohvatiRBSljedecaStanica(idVoznja));
            inkrementirajRBSljedecaStanica(idVoznja);
            ps.setInt(4, adresaNajblizeLokacije);
            ps.executeUpdate();

            ps2.setInt(1, idVoznja);
            ps2.setInt(2, idPaketaNajblizaAdresa);
            ps2.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String dohvatiVoziloVozaca(String string) {
        String query = "select RegistracioniBroj from Vozi where Vozac = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString(1);
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void azurirajLokacijuVozaca(int idAdr, String string) {
        String query = "update Vozi set Lokacija = ? where Vozac = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idAdr);
            ps.setString(2, string);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void azurirajLokacijuSvihPaketaUVozilu(String registracioniBroj, int idAdr) {
        String query = "select IdPak from UVozilu where RegistracioniBroj = ?";
        String query2 = "update Paket set Lokacija = ? where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            List<Integer> listaPaketaUVozilu = new ArrayList<>();
            ps.setString(1, registracioniBroj);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaPaketaUVozilu.add(rs.getInt(1));
            }

            for (int i = 0; i < listaPaketaUVozilu.size(); i++) {   //Azurira lokacije svih paketa u vozilu
                ps2.setInt(1, idAdr);
                ps2.setInt(2, listaPaketaUVozilu.get(i));
                ps2.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void ubaciPaketUVozilo(int idPak, int idAdr, String registracioniBroj) {
        String query = "insert into UVozilu(IdPak, RegistracioniBroj) values(?, ?)";
        String query2 = "update Paket set Status = 2, Lokacija = ?, IdMag = null where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, idPak);
            ps.setString(2, registracioniBroj);
            ps.executeUpdate();

            ps2.setInt(1, idAdr);
            ps2.setInt(2, idPak);
            ps2.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void izbaciPaketIzVozila(int idPak) {
        String query = "delete from UVozilu where IdPak = ?";
        String query2 = "update Paket set Status = 3, Lokacija = null where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, idPak);
            ps.executeUpdate();

            ps2.setInt(1, idPak);
            ps2.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void azurirajBrojIsporucenihPaketaKurira(String string) {
        String query = "update Kurir set BrojIsporucenihPaketa = BrojIsporucenihPaketa + 1 where KorisnickoIme = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, string);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void ubaciUIsporuceno(int idVoznja, String string, int idPak) {
        String query = "select Cijena from Paket where IdPak = ?";
        String query2 = "insert into Isporuceno(IdVoznja, KorisnickoIme, IdPak, Cijena) values(?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, idPak);
            ResultSet rs = ps.executeQuery();
            rs.next();
            BigDecimal cijena = rs.getBigDecimal(1);

            ps2.setInt(1, idVoznja);
            ps2.setString(2, string);
            ps2.setInt(3, idPak);
            ps2.setBigDecimal(4, cijena);
            ps2.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void ubaciNeisporucenePaketeUMagacin(String registracioniBroj, int idMag, int adresaMagacina) {
        String query = "select IdPak from UVozilu where RegistracioniBroj = ?";
        String query2 = "update Paket set IdMag = ?, Lokacija = ? where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            List<Integer> listaPaketaUVozilu = new ArrayList<>();
            ps.setString(1, registracioniBroj);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idPak = rs.getInt(1);
                listaPaketaUVozilu.add(idPak);
            }

            for (int i = 0; i < listaPaketaUVozilu.size(); i++) {
                ps2.setInt(1, idMag);
                ps2.setInt(2, adresaMagacina);
                ps2.setInt(3, listaPaketaUVozilu.get(i));
                ps2.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void azurirajStatusIProfitKurira(String registracioniBroj, String string) {
        String query = "update Kurir set Status = 0, Profit = ? where KorisnickoIme = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            BigDecimal profit = izracunajProfit(string, registracioniBroj);

            ps.setBigDecimal(1, profit);
            ps.setString(2, string);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void izbaciSvePaketeIzVozila(String registracioniBroj) {
        String query = "delete from UVozilu where RegistracioniBroj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, registracioniBroj);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void parkirajVoziloUMagacin(String string, int idMag, String registracioniBroj) {
        String query = "delete from Vozi where Vozac = ?";
        String query2 = "update Vozilo set idMag = ? where RegistracioniBroj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ps.executeUpdate();

            ps2.setInt(1, idMag);
            ps2.setString(2, registracioniBroj);
            ps2.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void azurirajPredjeniPut(int lokacijaVozaca, int idAdr, int idVoznja) {
        String query = "update Voznja set PredjeniPut = PredjeniPut + ? where IdVoznja = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            BigDecimal euklidskaDistanca = izracunajEuklidskuDistancu(lokacijaVozaca, idAdr);
            ps.setBigDecimal(1, euklidskaDistanca);
            ps.setInt(2, idVoznja);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyDriveOperation.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
