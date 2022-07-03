package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.PackageOperations;

public class ln180648_MyPackageOperations implements PackageOperations {

    private Connection conn;

    ln180648_MyPackageOperations(Connection conn) {
        this.conn = conn;
    }

    @Override
    public int insertPackage(int i, int i1, String string, int i2, BigDecimal bd) {
//        System.out.println("rs.etf.sab.student.ln180648_MyPackageOperations.insertPackage()");
        String query = "select IdAdr from Adresa where IdAdr = ?";
        String query2 = "select KorisnickoIme from Korisnik where KorisnickoIme = ?";
        String query3 = "select dbo.fIzracunajCijenuPaketa(?, ?, ?, ?)";
        String query4 = "insert into Paket(Status, Cijena, VrijemeKreiranja, Tip, Tezina, IdAdrSa, IdAdrNa, KorisnickoIme) values(0, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3);
                PreparedStatement ps4 = conn.prepareStatement(query4, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji AdrSa
                ps.setInt(1, i1);
                ResultSet rs12 = ps.executeQuery();
                if (rs12.next()) {  //Postoji AdrNa
                    ps2.setString(1, string);
                    ResultSet rs2 = ps2.executeQuery();
                    if (rs2.next()) { //Postoji korisnik sa datim korImenom

//                        BigDecimal cijenaPaketa = izracunajCijenuPaketa(i, i1, i2, bd);
                        ps3.setInt(1, i);
                        ps3.setInt(2, i1);
                        ps3.setInt(3, i2);
                        ps3.setBigDecimal(4, bd);
                        ResultSet rs3 = ps3.executeQuery();
                        rs3.next();
                        BigDecimal cijenaPaketa = rs3.getBigDecimal(1);

                        ps4.setBigDecimal(1, cijenaPaketa);
                        ps4.setTimestamp(2, new Timestamp(System.currentTimeMillis())); //OVO JE PROMJENA
                        ps4.setInt(3, i2);
                        ps4.setBigDecimal(4, bd);
                        ps4.setInt(5, i);
                        ps4.setInt(6, i1);
                        ps4.setString(7, string);
                        ps4.executeUpdate();
                        ResultSet rs4 = ps4.getGeneratedKeys();
                        rs4.next();
//                        System.out.println("Unijet paket " + rs4.getInt(1) + " adresaSa " + i + " adresaNa " + i1);
                        int idGrada = dohvatiIdGrada(i);    //POMOCNO
                        getAllPackagesCurrentlyAtCity(idGrada); //POMOCNO
                        return rs4.getInt(1);
                    } else {  //Ne postoji korisnik sa datim korImenom
                        return - 1;
                    }
                } else {  //Ne postoji AdrNa
                    return -1;
                }
            } else {  //Ne postoji AdrSa
                return -1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    @Override
    public boolean acceptAnOffer(int i) {
        String query = "select IdPak from Paket where IdPak = ?";
        String query2 = "update Paket set Status = 1, VrijemePrihvatanja = ? where IdPak = ?";
        String query3 = "update Ponuda set Status = 1 where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    //Postoji paket sa datim id
                ps2.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps2.setInt(2, i);
                ps2.executeUpdate();
                ps3.setInt(1, i);
                ps3.executeUpdate();
                azurirajBrojPoslatihPosiljkiKorisnika(i);
                return true;
            } else {    //Ne postoji paket sa datim id
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean rejectAnOffer(int i) {
        String query = "select IdPak from Paket where IdPak = ?";
        String query2 = "update Paket set Status = 4 where IdPak = ?";
        String query3 = "update Ponuda set Status = 2 where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    //Postoji paket sa datim id
                ps2.setInt(1, i);
                ps2.executeUpdate();
                ps3.setInt(1, i);
                ps3.executeUpdate();
                return true;
            } else {    //Ne postoji paket sa datim id
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public List<Integer> getAllPackages() {
        String query = "select IdPak from Paket";
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(query)) {
            List<Integer> listaPaketa = new ArrayList<>();
            while (rs.next()) {
                listaPaketa.add(rs.getInt(1));
            }
            return listaPaketa;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<Integer> getAllPackagesWithSpecificType(int i) {
        String query = "select IdPak from Paket where Tip = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            List<Integer> listaPaketa = new ArrayList<>();
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaPaketa.add(rs.getInt(1));
            }
            return listaPaketa;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<Integer> getAllUndeliveredPackages() {
//        System.out.println("rs.etf.sab.student.ln180648_MyPackageOperations.getAllUndeliveredPackages()");
        String query = "select IdPak from Paket where Status = 1 or Status = 2";
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(query)) {
            List<Integer> listaPaketa = new ArrayList<>();
            while (rs.next()) {
                listaPaketa.add(rs.getInt(1));
            }
            return listaPaketa;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<Integer> getAllUndeliveredPackagesFromCity(int i) {
//        System.out.println("rs.etf.sab.student.ln180648_MyPackageOperations.getAllUndeliveredPackagesFromCity()");
        String query = "select IdPak from Paket where IdAdrSa = ?";
        String query2 = "select IdPak from Paket where (Status = 1 or Status = 2) and IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            List<Integer> listaPaketa = new ArrayList<>();
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ps2.setInt(1, rs.getInt(1));
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) {
                    listaPaketa.add(rs2.getInt(1));
                }
            }
            return listaPaketa;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<Integer> getAllPackagesCurrentlyAtCity(int i) {
//        System.out.println("rs.etf.sab.student.ln180648_MyPackageOperations.getAllPackagesCurrentlyAtCity()");
//        System.out.println("Grad " + i);
        String query = "select IdAdr from Adresa where IdGrad = ?";
        String query2 = "select IdPak from Paket where (IdAdrSa = ? and (Status = 0 or Status = 1 or Status = 4)) or (IdAdrNa = ? and Status = 3)";
        String query3 = "select IdPak from Paket where Status = 2 and IdMag is not null";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                Statement s3 = conn.createStatement()) {
            List<Integer> listaPaketaIzGrada = new ArrayList<>();
            List<Integer> listaAdresaIzGrada = new ArrayList<>();
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaAdresaIzGrada.add(rs.getInt(1));
            }
            for (int j = 0; j < listaAdresaIzGrada.size(); j++) {
                ps2.setInt(1, listaAdresaIzGrada.get(j));
                ps2.setInt(2, listaAdresaIzGrada.get(j));
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    listaPaketaIzGrada.add(rs2.getInt(1));
//                    System.out.println("Paket " + rs2.getInt(1));
                }
            }
            ResultSet rs3 = s3.executeQuery(query3);
            while (rs3.next()) {
                listaPaketaIzGrada.add(rs3.getInt(1));
//                System.out.println("Paket " + rs3.getInt(1));
            }

            return listaPaketaIzGrada;

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public boolean deletePackage(int i) {
        String query = "select IdPak from Paket where IdPak = ? and (Status = 0 or Status = 4)";
        String query2 = "delete from Paket where IdPak = ?";
        String query3 = "delete from Ponuda where IdPak = ?";
        String query4 = "delete from Paket where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3);
                PreparedStatement ps4 = conn.prepareStatement(query4)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    //Postoji paket sa datim id i u statusu je zahtjev kreiran ili ponuda odbijena 
                ps2.setInt(1, i);
                ps2.executeUpdate();
                ps3.setInt(1, i);
                ps3.executeUpdate();
                ps4.setInt(1, i);
                ps4.executeUpdate();
                return true;
            } else {    //Ne postoji paket sa datim id ili nije u statusu zahtjev kreiran ili ponuda odbijena
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean changeWeight(int i, BigDecimal bd) {
        String query = "select IdPak from Paket where IdPak = ? and Status = 0";
        String query2 = "update Paket set Tezina = ? where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji paket sa datim id i u statusu je kreiran
                ps2.setBigDecimal(1, bd);
                ps2.setInt(2, i);
                ps2.executeUpdate();
                return true;
            } else {  //Ne postoji paket sa datim id ili nije u statusu kreiran
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean changeType(int i, int i1) {
        String query = "select IdPak from Paket where IdPak = ? and Status = 0";
        String query2 = "update Paket set Tip = ? where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji paket sa datim id i u statusu je kreiran
                ps2.setInt(1, i1);
                ps2.setInt(2, i);
                ps2.executeUpdate();
                return true;
            } else {  //Ne postoji paket sa datim id ili nije u statusu kreiran
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public int getDeliveryStatus(int i) {
//        System.out.println("rs.etf.sab.student.ln180648_MyPackageOperations.getDeliveryStatus()");
        String query = "select Status from Paket where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji paket sa datim id
//                System.out.println("Postoji paket sa datim id");
                return rs.getInt(1);
            } else {  //Ne postoji paket sa datim id
                return -1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    @Override
    public BigDecimal getPriceOfDelivery(int i) {
        String query = "select Cijena from Paket where IdPak = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji paket sa datim id 
                return rs.getBigDecimal(1);
            } else {  //Ne postoji paket sa datim id 
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public int getCurrentLocationOfPackage(int i) {
//        System.out.println("rs.etf.sab.student.ln180648_MyPackageOperations.getCurrentLocationOfPackage()");
        String query = "select IdPak from UVozilu where IdPak = ?";
        String query2 = "select Status from Paket where IdPak = ?";
        String query3 = "select Lokacija from Paket where IdPak = ?";
        String query4 = "select IdAdrNa from Paket where IdPak = ?";
        String query5 = "select IdAdrSa from Paket where IdPak = ?";
        String query6 = "select IdGrad from Adresa where IdAdr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3);
                PreparedStatement ps4 = conn.prepareStatement(query4);
                PreparedStatement ps5 = conn.prepareStatement(query5);
                PreparedStatement ps6 = conn.prepareStatement(query6)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { //Paket nije u vozilu
                ps2.setInt(1, i);
                ResultSet rs2 = ps2.executeQuery();
                rs2.next();
                int status = rs2.getInt(1);
                if (status == 2) {    //Paket je u statusu preuzet
                    ps3.setInt(1, i);
                    ResultSet rs3 = ps3.executeQuery();
                    rs3.next();
                    int idAdr = rs3.getInt(1);
                    ps6.setInt(1, idAdr);
                    ResultSet rs6 = ps6.executeQuery();
                    rs6.next();
                    return rs6.getInt(1);
                } else if (status == 3) { //Paket je u statusu isporucen
                    ps4.setInt(1, i);
                    ResultSet rs4 = ps4.executeQuery();
                    rs4.next();
                    int idAdr = rs4.getInt(1);
                    ps6.setInt(1, idAdr);
                    ResultSet rs6 = ps6.executeQuery();
                    rs6.next();
                    return rs6.getInt(1);
                } else { //Paket nije preuzet i nije isporucen, tj. nalazi se na pocetnoj adresi
                    ps5.setInt(1, i);
                    ResultSet rs5 = ps5.executeQuery();
                    rs5.next();
                    int idAdr = rs5.getInt(1);
                    ps6.setInt(1, idAdr);
                    ResultSet rs6 = ps6.executeQuery();
                    rs6.next();
                    return rs6.getInt(1);
                }
            } else { //Paket je u vozilu
                return -1;
            }
        } catch (SQLException ex) {
            return -2;
        }
    }

    @Override
    public Date getAcceptanceTime(int i) {
        String query = "select VrijemePrihvatanja from Paket where IdPak = ? and Status = 1";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji paket sa datim id i ponuda je prihvacena
                return rs.getDate(1);
            } else {  //Ne postoji paket sa datim id ili ponuda nije prihvacena
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public BigDecimal izracunajCijenuPaketa(int i, int i1, int i2, BigDecimal bd) {
        String query = "select XKoordinata, YKoordinata from Adresa where IdAdr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            BigDecimal cijenaPaketa;

            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int xAdrSa = rs.getInt(1);
            int yAdrSa = rs.getInt(2);

            ps.setInt(1, i1);
            ResultSet rs12 = ps.executeQuery();
            rs12.next();
            int xAdrNa = rs12.getInt(1);
            int yAdrNa = rs12.getInt(2);

            BigDecimal osnovnaCijena = new BigDecimal(0);
            BigDecimal cijenaPoKg = new BigDecimal(0);
            BigDecimal euklidskaDistanca;
            switch (i2) {
                case 0: {
                    osnovnaCijena = new BigDecimal(115);
                    cijenaPoKg = new BigDecimal(0);
                    break;
                }
                case 1: {
                    osnovnaCijena = new BigDecimal(175);
                    cijenaPoKg = new BigDecimal(100);
                    break;
                }
                case 2: {
                    osnovnaCijena = new BigDecimal(250);
                    cijenaPoKg = new BigDecimal(100);
                    break;
                }
                case 3: {
                    osnovnaCijena = new BigDecimal(350);
                    cijenaPoKg = new BigDecimal(500);
                    break;
                }
            }

            euklidskaDistanca = new BigDecimal(Math.sqrt(Math.pow(xAdrNa - xAdrSa, 2) + Math.pow(yAdrNa - yAdrSa, 2)));
//            System.out.println("euklidska distanca " + euklidskaDistanca);
            cijenaPaketa = osnovnaCijena.add(bd.multiply(cijenaPoKg)).multiply(euklidskaDistanca);
            return cijenaPaketa;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public int dohvatiIdGrada(int idAdr) { //OVO OBRISATI, SAMO POMOCNA FUNKCIJA
        String query = "select IdGrad from Adresa where IdAdr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idAdr);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public void azurirajBrojPoslatihPosiljkiKorisnika(int i) {
        String query = "select KorisnickoIme from Paket where IdPak = ?";
        String query2 = "update Korisnik set BrPoslatihPosiljki = BrPoslatihPosiljki + 1 where KorisnickoIme = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String korIme = rs.getString(1);
            ps2.setString(1, korIme);
            ps2.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyPackageOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
