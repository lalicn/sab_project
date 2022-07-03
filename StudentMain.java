package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.*;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;

public class StudentMain {

    public static void main(String[] args) {
        Connection conn = DB.getInstance().getConnection();
        
        AddressOperations addressOperations = new ln180648_MyAddressOperations(conn); // Change this to your implementation.
        CityOperations cityOperations = new ln180648_MyCityOperations(conn); // Do it for all classes.
        CourierOperations courierOperations = new ln180648_MyCourierOperations(conn); // e.g. = new MyDistrictOperations();
        CourierRequestOperation courierRequestOperation = new ln180648_MyCourierRequestOperation(conn);
        DriveOperation driveOperation = new ln180648_MyDriveOperation(conn);
        GeneralOperations generalOperations = new ln180648_MyGeneralOperations(conn);
        PackageOperations packageOperations = new ln180648_MyPackageOperations(conn);
        StockroomOperations stockroomOperations = new ln180648_MyStockroomOperations(conn);
        UserOperations userOperations = new ln180648_MyUserOperations(conn);
        VehicleOperations vehicleOperations = new ln180648_MyVehicleOperations(conn);

        TestHandler.createInstance(
                addressOperations,
                cityOperations,
                courierOperations,
                courierRequestOperation,
                driveOperation,
                generalOperations,
                packageOperations,
                stockroomOperations,
                userOperations,
                vehicleOperations);

        TestRunner.runTests();
        
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(StudentMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
