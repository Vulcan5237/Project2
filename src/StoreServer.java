import com.google.gson.Gson;
import java.io.PrintWriter;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Scanner;

public class StoreServer {
    static String dbfile = "C:\\Users\\grant\\Documents\\Databases\\store.db";


    public static void main(String[] args) {

        int port = 1001;

        if (args.length > 0) {
            System.out.println("Running arguments: ");
            for (String arg : args)
                System.out.println(arg);
            port = Integer.parseInt(args[0]);
            dbfile = args[1];
        }

        try {
            SQLiteDataAdapter adapter = new SQLiteDataAdapter();
            Gson gson = new Gson();
            adapter.connect(dbfile);

            ServerSocket server = new ServerSocket(port);

            System.out.println("Server is listening at port = " + port);

            while (true) {
                Socket pipe = server.accept();
                PrintWriter out = new PrintWriter(pipe.getOutputStream(), true);
                Scanner in = new Scanner(pipe.getInputStream());

                MessageModel msg = gson.fromJson(in.nextLine(), MessageModel.class);

                if (msg.code == MessageModel.GET_PRODUCT) {
                    System.out.println("GET product with id = " + msg.data);
                    ProductModel p = adapter.loadProduct(Integer.parseInt(msg.data));
                    if (p == null) {
                        msg.code = MessageModel.OPERATION_FAILED;
                    }
                    else {
                        msg.code = MessageModel.OPERATION_OK; // load successfully!!!
                        msg.data = gson.toJson(p);
                    }
                    out.println(gson.toJson(msg));
                }

                if (msg.code == MessageModel.PUT_PRODUCT) {
                    ProductModel p = gson.fromJson(msg.data, ProductModel.class);
                    System.out.println("PUT command with Product = " + p);
                    int res = adapter.saveProduct(p);
                    if (res == IDataAdapter.PRODUCT_SAVE_OK) {
                        msg.code = MessageModel.OPERATION_OK;
                    }
                    else {
                        msg.code = MessageModel.OPERATION_FAILED;
                    }
                    out.println(gson.toJson(msg));
                }

                if (msg.code == MessageModel.GET_CUSTOMER) {
                    System.out.println("GET customer with id = " + msg.data);
                    CustomerModel c = adapter.loadCustomer(Integer.parseInt(msg.data));
                    if (c == null) {
                        msg.code = MessageModel.OPERATION_FAILED;
                    }
                    else {
                        msg.code = MessageModel.OPERATION_OK; // load successfully!!!
                        msg.data = gson.toJson(c);
                    }
                    out.println(gson.toJson(msg));
                }

                if (msg.code == MessageModel.PUT_CUSTOMER) {
                    //Step 1: build customer model from message
                    CustomerModel c = gson.fromJson(msg.data, CustomerModel.class);
                    System.out.println("PUT command with Customer = " + c);

                    //Use the sql adapter to actually edit database
                    int res = adapter.saveCustomer(c);

                    //test if it worked and return the result
                    if (res == IDataAdapter.CUSTOMER_SAVE_OK) {
                        msg.code = MessageModel.OPERATION_OK;
                    }
                    else {
                        msg.code = MessageModel.OPERATION_FAILED;
                    }
                    //Tell the UI the result
                    out.println(gson.toJson(msg));

                }

                if (msg.code == MessageModel.GET_PURCHASE) {
                    PurchaseModel p = adapter.loadPurchase(Integer.parseInt(msg.data));
                    if (p == null) {
                        msg.code = MessageModel.OPERATION_FAILED;
                    }
                    else {
                        msg.code = MessageModel.OPERATION_OK; // load successfully!!!
                        msg.data = gson.toJson(p);
                    }
                    out.println(gson.toJson(msg));
                }

                if (msg.code == MessageModel.PUT_PURCHASE) {
                    //Step 1: build customer model from message
                    PurchaseModel p = gson.fromJson(msg.data, PurchaseModel.class);
                    System.out.println("PUT command with Purchase = " + p);

                    //Use the sql adapter to actually edit database
                    int res = adapter.savePurchase(p);

                    //test if it worked and return the result
                    if (res == IDataAdapter.PURCHASE_SAVE_OK) {
                        msg.code = MessageModel.OPERATION_OK;
                    }
                    else {
                        msg.code = MessageModel.OPERATION_FAILED;
                    }
                    //Tell the UI the result
                    out.println(gson.toJson(msg));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}