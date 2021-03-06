USE [master]
GO
/****** Object:  Database [KurirskaSluzba]    Script Date: 31-May-22 10:18:36 PM ******/
CREATE DATABASE [KurirskaSluzba]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'KurirskaSluzba', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.MSSQLSERVER\MSSQL\DATA\KurirskaSluzba.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'KurirskaSluzba_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.MSSQLSERVER\MSSQL\DATA\KurirskaSluzba_log.ldf' , SIZE = 335872KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT
GO
ALTER DATABASE [KurirskaSluzba] SET COMPATIBILITY_LEVEL = 150
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [KurirskaSluzba].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [KurirskaSluzba] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET ARITHABORT OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [KurirskaSluzba] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [KurirskaSluzba] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET  DISABLE_BROKER 
GO
ALTER DATABASE [KurirskaSluzba] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [KurirskaSluzba] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET RECOVERY FULL 
GO
ALTER DATABASE [KurirskaSluzba] SET  MULTI_USER 
GO
ALTER DATABASE [KurirskaSluzba] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [KurirskaSluzba] SET DB_CHAINING OFF 
GO
ALTER DATABASE [KurirskaSluzba] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [KurirskaSluzba] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [KurirskaSluzba] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [KurirskaSluzba] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'KurirskaSluzba', N'ON'
GO
ALTER DATABASE [KurirskaSluzba] SET QUERY_STORE = OFF
GO
USE [KurirskaSluzba]
GO
/****** Object:  UserDefinedFunction [dbo].[fIzracunajCijenuPaketa]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE FUNCTION [dbo].[fIzracunajCijenuPaketa]
(
	@IdAdresaSa int, @IdAdresaNa int, @Tip int, @Tezina decimal(10,3)
)
RETURNS decimal(10,3)
AS
BEGIN
	declare @CijenaPaketa decimal(10,3);
	declare @OsnovaCijena int, @CijenaPoKg int, @XAdrSa int, @XAdrNa int, @YAdrSa int, @YAdrNa int;
	declare @EuklidskaDistanca decimal(10,3);
	
	set @OsnovaCijena = case @Tip
	when 0 then 115
	when 1 then 175
	when 2 then 250
	when 3 then 350
	end

	set @CijenaPoKg = case @Tip
	when 0 then 0
	when 1 then 100
	when 2 then 100
	when 3 then 500
	end

	select @XAdrSa = XKoordinata, @YAdrSa = YKoordinata
	from Adresa
	where IdAdr = @IdAdresaSa

	select @XAdrNa = XKoordinata, @YAdrNa = YKoordinata
	from Adresa
	where IdAdr = @IdAdresaNa

	set @EuklidskaDistanca = SQRT(SQUARE(@XAdrNa - @XAdrSa) + SQUARE(@YAdrNa - @YAdrSa))

	set @CijenaPaketa = (@OsnovaCijena + @Tezina * @CijenaPoKg) * @EuklidskaDistanca

	return @CijenaPaketa

END
GO
/****** Object:  Table [dbo].[Administrator]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Administrator](
	[KorisnickoIme] [varchar](100) NOT NULL,
 CONSTRAINT [PK_Administrator] PRIMARY KEY CLUSTERED 
(
	[KorisnickoIme] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Adresa]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Adresa](
	[IdAdr] [int] IDENTITY(1,1) NOT NULL,
	[Ulica] [varchar](100) NOT NULL,
	[Broj] [int] NOT NULL,
	[IdGrad] [int] NOT NULL,
	[XKoordinata] [int] NOT NULL,
	[YKoordinata] [int] NOT NULL,
 CONSTRAINT [PK_Adresa_1] PRIMARY KEY CLUSTERED 
(
	[IdAdr] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Grad]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Grad](
	[IdGrad] [int] IDENTITY(1,1) NOT NULL,
	[PostanskiBroj] [varchar](100) NOT NULL,
	[Naziv] [varchar](100) NOT NULL,
 CONSTRAINT [XPKGrad] PRIMARY KEY CLUSTERED 
(
	[IdGrad] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Isporuceno]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Isporuceno](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[IdVoznja] [int] NOT NULL,
	[KorisnickoIme] [varchar](100) NOT NULL,
	[IdPak] [int] NOT NULL,
	[Cijena] [decimal](10, 3) NOT NULL,
 CONSTRAINT [PK_Isporuceno] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Korisnik]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Korisnik](
	[KorisnickoIme] [varchar](100) NOT NULL,
	[Ime] [varchar](100) NOT NULL,
	[Prezime] [varchar](100) NOT NULL,
	[Sifra] [varchar](100) NOT NULL,
	[IdAdr] [int] NOT NULL,
	[BrPoslatihPosiljki] [int] NULL,
 CONSTRAINT [PK_Korisnik_1] PRIMARY KEY CLUSTERED 
(
	[KorisnickoIme] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [IX_Korisnik] UNIQUE NONCLUSTERED 
(
	[KorisnickoIme] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Kupac]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Kupac](
	[KorisnickoIme] [varchar](100) NOT NULL,
 CONSTRAINT [PK_Kupac] PRIMARY KEY CLUSTERED 
(
	[KorisnickoIme] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Kurir]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Kurir](
	[KorisnickoIme] [varchar](100) NOT NULL,
	[BrojIsporucenihPaketa] [int] NOT NULL,
	[Profit] [decimal](10, 3) NOT NULL,
	[Status] [int] NOT NULL,
	[BrVozackeDozvole] [varchar](100) NOT NULL,
 CONSTRAINT [PK_Kurir_1] PRIMARY KEY CLUSTERED 
(
	[KorisnickoIme] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Magacin]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Magacin](
	[IdMag] [int] IDENTITY(1,1) NOT NULL,
	[IdAdr] [int] NOT NULL,
	[Status] [int] NOT NULL,
 CONSTRAINT [PK_Magacin] PRIMARY KEY CLUSTERED 
(
	[IdMag] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Paket]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Paket](
	[IdPak] [int] IDENTITY(1,1) NOT NULL,
	[Status] [int] NOT NULL,
	[VrijemeKreiranja] [datetime] NOT NULL,
	[VrijemePrihvatanja] [datetime] NULL,
	[Lokacija] [int] NULL,
	[Tip] [int] NOT NULL,
	[Tezina] [decimal](10, 3) NOT NULL,
	[IdMag] [int] NULL,
	[IdAdrSa] [int] NOT NULL,
	[IdAdrNa] [int] NOT NULL,
	[KorisnickoIme] [varchar](100) NOT NULL,
	[Cijena] [decimal](10, 3) NULL,
 CONSTRAINT [PK_Paket] PRIMARY KEY CLUSTERED 
(
	[IdPak] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PlanIsporuke]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PlanIsporuke](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[IdVoznja] [int] NOT NULL,
	[IdPak] [int] NOT NULL,
	[Status] [int] NOT NULL,
	[RedniBroj] [int] NOT NULL,
	[IdAdr] [int] NOT NULL,
 CONSTRAINT [PK_PlanIsporuke] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PlanPreuzimanja]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PlanPreuzimanja](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[IdVoznja] [int] NOT NULL,
	[IdPak] [int] NOT NULL,
	[IdGrad] [int] NOT NULL,
	[IdAdr] [int] NOT NULL,
	[Stop] [int] NOT NULL,
	[Status] [int] NOT NULL,
	[RedniBroj] [int] NOT NULL,
 CONSTRAINT [PK_PlanPreuzimanjaIzGrada] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Ponuda]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Ponuda](
	[IdPak] [int] NOT NULL,
	[Status] [int] NOT NULL,
	[CijenaIsporuke] [decimal](10, 3) NOT NULL,
 CONSTRAINT [PK_Ponuda] PRIMARY KEY CLUSTERED 
(
	[IdPak] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[UVozilu]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[UVozilu](
	[IdPak] [int] NOT NULL,
	[RegistracioniBroj] [varchar](100) NOT NULL,
 CONSTRAINT [PK_UVozilu] PRIMARY KEY CLUSTERED 
(
	[IdPak] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Vozi]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Vozi](
	[Vozac] [varchar](100) NOT NULL,
	[RegistracioniBroj] [varchar](100) NOT NULL,
	[Lokacija] [int] NOT NULL,
	[IdVoznja] [int] NOT NULL,
 CONSTRAINT [PK_Vozi] PRIMARY KEY CLUSTERED 
(
	[Vozac] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Vozilo]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Vozilo](
	[RegistracioniBroj] [varchar](100) NOT NULL,
	[TipGoriva] [int] NOT NULL,
	[Potrosnja] [decimal](10, 3) NOT NULL,
	[Nosivost] [decimal](10, 3) NOT NULL,
	[IdMag] [int] NULL,
 CONSTRAINT [PK_Vozilo_1] PRIMARY KEY CLUSTERED 
(
	[RegistracioniBroj] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Voznja]    Script Date: 31-May-22 10:18:36 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Voznja](
	[IdVoznja] [int] IDENTITY(1,1) NOT NULL,
	[RegistracioniBroj] [varchar](100) NOT NULL,
	[Vozac] [varchar](100) NOT NULL,
	[PredjeniPut] [decimal](10, 3) NOT NULL,
	[RBSljedecaStanica] [int] NOT NULL,
	[UkupnaTezinaUVozilu] [decimal](10, 3) NOT NULL,
 CONSTRAINT [PK_Voznja] PRIMARY KEY CLUSTERED 
(
	[IdVoznja] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_Vozi]    Script Date: 31-May-22 10:18:36 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [IX_Vozi] ON [dbo].[Vozi]
(
	[RegistracioniBroj] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Korisnik] ADD  CONSTRAINT [DF_Korisnik_BrPoslatihPosiljki]  DEFAULT ((0)) FOR [BrPoslatihPosiljki]
GO
ALTER TABLE [dbo].[Kurir] ADD  CONSTRAINT [DF_Kurir_BrojIsporucenihPaketa]  DEFAULT ((0)) FOR [BrojIsporucenihPaketa]
GO
ALTER TABLE [dbo].[Kurir] ADD  CONSTRAINT [DF_Kurir_Profit]  DEFAULT ((0)) FOR [Profit]
GO
ALTER TABLE [dbo].[Kurir] ADD  CONSTRAINT [DF_Kurir_Status]  DEFAULT ((0)) FOR [Status]
GO
ALTER TABLE [dbo].[Magacin] ADD  CONSTRAINT [DF_Magacin_Status]  DEFAULT ((0)) FOR [Status]
GO
ALTER TABLE [dbo].[PlanIsporuke] ADD  CONSTRAINT [DF_PlanIsporuke_Status]  DEFAULT ((0)) FOR [Status]
GO
ALTER TABLE [dbo].[PlanPreuzimanja] ADD  CONSTRAINT [DF_PlanPreuzimanjaIzGrada_Status]  DEFAULT ((0)) FOR [Status]
GO
ALTER TABLE [dbo].[Ponuda] ADD  CONSTRAINT [DF_Ponuda_Status]  DEFAULT ((0)) FOR [Status]
GO
ALTER TABLE [dbo].[Voznja] ADD  CONSTRAINT [DF_Voznja_PredjeniPut]  DEFAULT ((0)) FOR [PredjeniPut]
GO
ALTER TABLE [dbo].[Voznja] ADD  CONSTRAINT [DF_Voznja_RBrSljedecaStanica]  DEFAULT ((1)) FOR [RBSljedecaStanica]
GO
ALTER TABLE [dbo].[Voznja] ADD  CONSTRAINT [DF_Voznja_UkupnaTezinaUVozilu]  DEFAULT ((0)) FOR [UkupnaTezinaUVozilu]
GO
ALTER TABLE [dbo].[Administrator]  WITH CHECK ADD  CONSTRAINT [FK_Administrator_Korisnik] FOREIGN KEY([KorisnickoIme])
REFERENCES [dbo].[Korisnik] ([KorisnickoIme])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Administrator] CHECK CONSTRAINT [FK_Administrator_Korisnik]
GO
ALTER TABLE [dbo].[Adresa]  WITH CHECK ADD  CONSTRAINT [FK_Adresa_Grad] FOREIGN KEY([IdGrad])
REFERENCES [dbo].[Grad] ([IdGrad])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Adresa] CHECK CONSTRAINT [FK_Adresa_Grad]
GO
ALTER TABLE [dbo].[Isporuceno]  WITH CHECK ADD  CONSTRAINT [FK_Isporuceno_Kurir] FOREIGN KEY([KorisnickoIme])
REFERENCES [dbo].[Kurir] ([KorisnickoIme])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Isporuceno] CHECK CONSTRAINT [FK_Isporuceno_Kurir]
GO
ALTER TABLE [dbo].[Isporuceno]  WITH CHECK ADD  CONSTRAINT [FK_Isporuceno_Paket] FOREIGN KEY([IdPak])
REFERENCES [dbo].[Paket] ([IdPak])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Isporuceno] CHECK CONSTRAINT [FK_Isporuceno_Paket]
GO
ALTER TABLE [dbo].[Isporuceno]  WITH CHECK ADD  CONSTRAINT [FK_Isporuceno_Voznja] FOREIGN KEY([IdVoznja])
REFERENCES [dbo].[Voznja] ([IdVoznja])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Isporuceno] CHECK CONSTRAINT [FK_Isporuceno_Voznja]
GO
ALTER TABLE [dbo].[Korisnik]  WITH CHECK ADD  CONSTRAINT [FK_Korisnik_Adresa] FOREIGN KEY([IdAdr])
REFERENCES [dbo].[Adresa] ([IdAdr])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Korisnik] CHECK CONSTRAINT [FK_Korisnik_Adresa]
GO
ALTER TABLE [dbo].[Kupac]  WITH CHECK ADD  CONSTRAINT [FK_Kupac_Korisnik1] FOREIGN KEY([KorisnickoIme])
REFERENCES [dbo].[Korisnik] ([KorisnickoIme])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Kupac] CHECK CONSTRAINT [FK_Kupac_Korisnik1]
GO
ALTER TABLE [dbo].[Kurir]  WITH CHECK ADD  CONSTRAINT [FK_Kurir_Korisnik1] FOREIGN KEY([KorisnickoIme])
REFERENCES [dbo].[Korisnik] ([KorisnickoIme])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Kurir] CHECK CONSTRAINT [FK_Kurir_Korisnik1]
GO
ALTER TABLE [dbo].[Magacin]  WITH CHECK ADD  CONSTRAINT [FK_Magacin_Adresa] FOREIGN KEY([IdAdr])
REFERENCES [dbo].[Adresa] ([IdAdr])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Magacin] CHECK CONSTRAINT [FK_Magacin_Adresa]
GO
ALTER TABLE [dbo].[Paket]  WITH CHECK ADD  CONSTRAINT [FK_Paket_Adresa] FOREIGN KEY([Lokacija])
REFERENCES [dbo].[Adresa] ([IdAdr])
GO
ALTER TABLE [dbo].[Paket] CHECK CONSTRAINT [FK_Paket_Adresa]
GO
ALTER TABLE [dbo].[Paket]  WITH CHECK ADD  CONSTRAINT [FK_Paket_Adresa1] FOREIGN KEY([IdAdrSa])
REFERENCES [dbo].[Adresa] ([IdAdr])
GO
ALTER TABLE [dbo].[Paket] CHECK CONSTRAINT [FK_Paket_Adresa1]
GO
ALTER TABLE [dbo].[Paket]  WITH CHECK ADD  CONSTRAINT [FK_Paket_Adresa2] FOREIGN KEY([IdAdrNa])
REFERENCES [dbo].[Adresa] ([IdAdr])
GO
ALTER TABLE [dbo].[Paket] CHECK CONSTRAINT [FK_Paket_Adresa2]
GO
ALTER TABLE [dbo].[Paket]  WITH CHECK ADD  CONSTRAINT [FK_Paket_Korisnik] FOREIGN KEY([KorisnickoIme])
REFERENCES [dbo].[Korisnik] ([KorisnickoIme])
GO
ALTER TABLE [dbo].[Paket] CHECK CONSTRAINT [FK_Paket_Korisnik]
GO
ALTER TABLE [dbo].[Paket]  WITH CHECK ADD  CONSTRAINT [FK_Paket_Magacin] FOREIGN KEY([IdMag])
REFERENCES [dbo].[Magacin] ([IdMag])
GO
ALTER TABLE [dbo].[Paket] CHECK CONSTRAINT [FK_Paket_Magacin]
GO
ALTER TABLE [dbo].[PlanIsporuke]  WITH CHECK ADD  CONSTRAINT [FK_PlanIsporuke_Adresa] FOREIGN KEY([IdAdr])
REFERENCES [dbo].[Adresa] ([IdAdr])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[PlanIsporuke] CHECK CONSTRAINT [FK_PlanIsporuke_Adresa]
GO
ALTER TABLE [dbo].[PlanIsporuke]  WITH CHECK ADD  CONSTRAINT [FK_PlanIsporuke_Paket] FOREIGN KEY([IdPak])
REFERENCES [dbo].[Paket] ([IdPak])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[PlanIsporuke] CHECK CONSTRAINT [FK_PlanIsporuke_Paket]
GO
ALTER TABLE [dbo].[PlanIsporuke]  WITH CHECK ADD  CONSTRAINT [FK_PlanIsporuke_Voznja] FOREIGN KEY([IdVoznja])
REFERENCES [dbo].[Voznja] ([IdVoznja])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[PlanIsporuke] CHECK CONSTRAINT [FK_PlanIsporuke_Voznja]
GO
ALTER TABLE [dbo].[PlanPreuzimanja]  WITH CHECK ADD  CONSTRAINT [FK_PlanPreuzimanja_Adresa] FOREIGN KEY([IdAdr])
REFERENCES [dbo].[Adresa] ([IdAdr])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[PlanPreuzimanja] CHECK CONSTRAINT [FK_PlanPreuzimanja_Adresa]
GO
ALTER TABLE [dbo].[PlanPreuzimanja]  WITH CHECK ADD  CONSTRAINT [FK_PlanPreuzimanja_Grad] FOREIGN KEY([IdGrad])
REFERENCES [dbo].[Grad] ([IdGrad])
GO
ALTER TABLE [dbo].[PlanPreuzimanja] CHECK CONSTRAINT [FK_PlanPreuzimanja_Grad]
GO
ALTER TABLE [dbo].[PlanPreuzimanja]  WITH CHECK ADD  CONSTRAINT [FK_PlanPreuzimanja_Paket] FOREIGN KEY([IdPak])
REFERENCES [dbo].[Paket] ([IdPak])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[PlanPreuzimanja] CHECK CONSTRAINT [FK_PlanPreuzimanja_Paket]
GO
ALTER TABLE [dbo].[PlanPreuzimanja]  WITH CHECK ADD  CONSTRAINT [FK_PlanPreuzimanja_Voznja] FOREIGN KEY([IdVoznja])
REFERENCES [dbo].[Voznja] ([IdVoznja])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[PlanPreuzimanja] CHECK CONSTRAINT [FK_PlanPreuzimanja_Voznja]
GO
ALTER TABLE [dbo].[Ponuda]  WITH CHECK ADD  CONSTRAINT [FK_Ponuda_Paket] FOREIGN KEY([IdPak])
REFERENCES [dbo].[Paket] ([IdPak])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Ponuda] CHECK CONSTRAINT [FK_Ponuda_Paket]
GO
ALTER TABLE [dbo].[UVozilu]  WITH CHECK ADD  CONSTRAINT [FK_UVozilu_Paket] FOREIGN KEY([IdPak])
REFERENCES [dbo].[Paket] ([IdPak])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[UVozilu] CHECK CONSTRAINT [FK_UVozilu_Paket]
GO
ALTER TABLE [dbo].[UVozilu]  WITH CHECK ADD  CONSTRAINT [FK_UVozilu_Vozilo] FOREIGN KEY([RegistracioniBroj])
REFERENCES [dbo].[Vozilo] ([RegistracioniBroj])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[UVozilu] CHECK CONSTRAINT [FK_UVozilu_Vozilo]
GO
ALTER TABLE [dbo].[Vozi]  WITH CHECK ADD  CONSTRAINT [FK_Vozi_Adresa] FOREIGN KEY([Lokacija])
REFERENCES [dbo].[Adresa] ([IdAdr])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Vozi] CHECK CONSTRAINT [FK_Vozi_Adresa]
GO
ALTER TABLE [dbo].[Vozi]  WITH CHECK ADD  CONSTRAINT [FK_Vozi_Kurir] FOREIGN KEY([Vozac])
REFERENCES [dbo].[Kurir] ([KorisnickoIme])
GO
ALTER TABLE [dbo].[Vozi] CHECK CONSTRAINT [FK_Vozi_Kurir]
GO
ALTER TABLE [dbo].[Vozi]  WITH CHECK ADD  CONSTRAINT [FK_Vozi_Vozilo] FOREIGN KEY([RegistracioniBroj])
REFERENCES [dbo].[Vozilo] ([RegistracioniBroj])
GO
ALTER TABLE [dbo].[Vozi] CHECK CONSTRAINT [FK_Vozi_Vozilo]
GO
ALTER TABLE [dbo].[Vozi]  WITH CHECK ADD  CONSTRAINT [FK_Vozi_Voznja] FOREIGN KEY([IdVoznja])
REFERENCES [dbo].[Voznja] ([IdVoznja])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Vozi] CHECK CONSTRAINT [FK_Vozi_Voznja]
GO
ALTER TABLE [dbo].[Vozilo]  WITH CHECK ADD  CONSTRAINT [FK_Vozilo_Magacin] FOREIGN KEY([IdMag])
REFERENCES [dbo].[Magacin] ([IdMag])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[Vozilo] CHECK CONSTRAINT [FK_Vozilo_Magacin]
GO
ALTER TABLE [dbo].[Voznja]  WITH CHECK ADD  CONSTRAINT [FK_Voznja_Kurir] FOREIGN KEY([Vozac])
REFERENCES [dbo].[Kurir] ([KorisnickoIme])
GO
ALTER TABLE [dbo].[Voznja] CHECK CONSTRAINT [FK_Voznja_Kurir]
GO
ALTER TABLE [dbo].[Voznja]  WITH CHECK ADD  CONSTRAINT [FK_Voznja_Vozilo] FOREIGN KEY([RegistracioniBroj])
REFERENCES [dbo].[Vozilo] ([RegistracioniBroj])
GO
ALTER TABLE [dbo].[Voznja] CHECK CONSTRAINT [FK_Voznja_Vozilo]
GO
ALTER TABLE [dbo].[Kurir]  WITH CHECK ADD  CONSTRAINT [CK_Kurir] CHECK  (([Status]>=(0) AND [Status]<=(1)))
GO
ALTER TABLE [dbo].[Kurir] CHECK CONSTRAINT [CK_Kurir]
GO
ALTER TABLE [dbo].[Magacin]  WITH CHECK ADD  CONSTRAINT [CK_Magacin] CHECK  (([Status]>=(0) AND [Status]<=(1)))
GO
ALTER TABLE [dbo].[Magacin] CHECK CONSTRAINT [CK_Magacin]
GO
ALTER TABLE [dbo].[Paket]  WITH CHECK ADD  CONSTRAINT [CK_Paket] CHECK  (([Status]>=(0) AND [Status]<=(4)))
GO
ALTER TABLE [dbo].[Paket] CHECK CONSTRAINT [CK_Paket]
GO
ALTER TABLE [dbo].[Paket]  WITH CHECK ADD  CONSTRAINT [CK_Paket_1] CHECK  (([Tip]>=(0) AND [Tip]<=(3)))
GO
ALTER TABLE [dbo].[Paket] CHECK CONSTRAINT [CK_Paket_1]
GO
ALTER TABLE [dbo].[PlanIsporuke]  WITH CHECK ADD  CONSTRAINT [CK_PlanIsporuke] CHECK  (([Status]>=(0) AND [Status]<=(1)))
GO
ALTER TABLE [dbo].[PlanIsporuke] CHECK CONSTRAINT [CK_PlanIsporuke]
GO
ALTER TABLE [dbo].[PlanPreuzimanja]  WITH CHECK ADD  CONSTRAINT [CK_PlanPreuzimanjaIzGrada] CHECK  (([Status]>=(0) AND [Status]<=(1)))
GO
ALTER TABLE [dbo].[PlanPreuzimanja] CHECK CONSTRAINT [CK_PlanPreuzimanjaIzGrada]
GO
ALTER TABLE [dbo].[Ponuda]  WITH CHECK ADD  CONSTRAINT [CK_Ponuda] CHECK  (([Status]>=(0) AND [Status]<=(2)))
GO
ALTER TABLE [dbo].[Ponuda] CHECK CONSTRAINT [CK_Ponuda]
GO
ALTER TABLE [dbo].[Vozilo]  WITH CHECK ADD  CONSTRAINT [CK_Vozilo] CHECK  (([TipGoriva]>=(0) AND [TipGoriva]<=(2)))
GO
ALTER TABLE [dbo].[Vozilo] CHECK CONSTRAINT [CK_Vozilo]
GO
USE [master]
GO
ALTER DATABASE [KurirskaSluzba] SET  READ_WRITE 
GO
