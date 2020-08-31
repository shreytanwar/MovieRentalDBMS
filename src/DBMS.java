import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import net.proteanit.sql.DbUtils;

public class DBMS {

    static class userstable
    {
        String uid;
        String username;
        String password;
        String admin;

        userstable(String _uid,String _username, String _password, String _admin)
        {
            this.uid=_uid;
            this.username=_username;
            this.password=_password;
            this.admin=_admin;
        }
    }


    public static class ex{
        public static int days=0;
    }

    public static void main(String[] args){
        create();     //uncomment to  create DB (first time)
        login();        // logs in for the first time, that user will be an admin by default {admin, admin}.

    }

    //connect the database to the GUI(swing)
    public static Connection connect()
    {
        try
        {
            //Connecting to MySQL Using the JDBC DriverManager Interface
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/mysql?user=root&password=admin");
            return con;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    //create the database, tables and add data into these tables
    public static void create()
    {
        try
        {
            Connection connection=connect();


            ResultSet resultSet= connection.getMetaData().getCatalogs();
            while(resultSet.next())
            {
                String databaseName = resultSet.getString(1);
                //DBName at position 1

                if(databaseName.equals("moviedb"))
                {
                    Statement stmt = connection.createStatement();
                    //Drop db if already exists to reset
                    String sql = "DROP DATABASE moviedb";
                    stmt.executeUpdate(sql);
                }
            }

            Statement stmt = connection.createStatement();

            //create db
            String sql = "CREATE DATABASE moviedb";
            stmt.executeUpdate(sql);
            //use db
            stmt.executeUpdate("USE moviedb");

            //create users table
            String sql1 = "CREATE TABLE USERS(UIS INT NOT NULL AUTO_INCREMENT PRIMARY KEY, USERNAME VARCHAR(30) NOT NULL UNIQUE, PASSWORD VARCHAR(30), ADMIN BOOLEAN)";
            stmt.executeUpdate(sql1);

            //insert users //+check statement
            stmt.executeUpdate("INSERT INTO USERS(USERNAME,PASSWORD,ADMIN) VALUES('admin','admin','1')");

            //create movies table
            stmt.executeUpdate("CREATE TABLE MOVIES(MID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, MNAME VARCHAR(50), GENRE VARCHAR(20), PRICE INT)");

            //create issued table
            stmt.executeUpdate("CREATE TABLE ISSUED(IID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, UID INT, MID INT, ISSUED_DATE VARCHAR(20), RETURN_DATE VARCHAR(20), PERIOD INT, FINE INT)");

            //insert into movies table
            stmt.executeUpdate("INSERT INTO MOVIES(MNAME, GENRE, PRICE) VALUES ('The Butterfly Effect', 'Sci-Fi', 200),  ('The Guest movie', 'Fiction', 300), ('Joker','Crime', 150), ('Interstellar', ' Adventure', 250), ('The Godfather','Crime', 350), ('The Shawshank Redemption','Drama', 200)");

            resultSet.close();


        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void login()
    {
        JFrame primaryFrame=new JFrame("Login");
        JLabel l1,l2;

        //labels
        l1=new JLabel("Username");
        l1.setBounds(30,15,100,30);
        l2=new JLabel(("Password"));
        l2.setBounds(30,50,100,30);

        //textFields
        JTextField F_user = new JTextField();
        F_user.setBounds(110, 15 ,200, 30);
        JPasswordField F_pass = new JPasswordField();
        F_pass.setBounds(110, 50 ,200, 30);

        //button
        JButton login_but=new JButton("Login");
        login_but.setBounds(130,90,80,25);
        login_but.addActionListener(new ActionListener() {  //check action listener
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = F_user.getText();
                String password= F_pass.getText();

                //check empty
                if(username.equals(""))
                {
                    JOptionPane.showMessageDialog(null,"Enter username");
                }
                else if(password.equals(""))
                {
                    JOptionPane.showMessageDialog(null,"Enter password");
                }
                else{
                    //check if user details are correct
                    //connect to DB
                    Connection connection=connect();

                    try
                    {
                        Statement stmt=connection.createStatement();
                        stmt.executeUpdate("USE moviedb");     //use DB with name "moviedb"
                        stmt=connection.createStatement();
                        String st=("SELECT * FROM USERS WHERE USERNAME='"+username+"' AND PASSWORD='"+password+"'");    //get username and password from db
                        ResultSet rs= stmt.executeQuery(st); //execute query

                        ArrayList<userstable> userlist= new ArrayList<>();
                        try
                        {
                            while (rs.next())
                            {
                                userstable temp=new userstable(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4));
                                userlist.add(temp);
                            }
                        }
                        catch(SQLException q)
                        {

                        }

                        if(userlist.isEmpty())
                        {
                            System.out.print("No user");
                            JOptionPane.showMessageDialog(null,"Wrong Username or Password!");
                        }
                        else
                        {
                            //remove current frame
                            primaryFrame.dispose();
                            //rs.beforeFirst(); //Move pointer to top
                            for(int i=0; i<userlist.size();i++)
                            {
                                String admin=userlist.get(i).admin;

                                String UID=userlist.get(i).uid;

                                if(admin.equals("1"))
                                {
                                    admin_menu();   //opens admin menu
                                }
                                else
                                {
                                    user_menu(UID); //opens user menu for uid user
                                }

                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        });

        primaryFrame.add(login_but);
        primaryFrame.add(F_user);
        primaryFrame.add(F_pass);
        primaryFrame.add(l1);
        primaryFrame.add(l2);



        primaryFrame.setSize(400,200);
        primaryFrame.setLayout(null);
        primaryFrame.setVisible(true);
        primaryFrame.setLocationRelativeTo(null);
        primaryFrame.setResizable(false);
    }

    // show details of all the movies present in the moviedb and the movies issued by the user.
    public static void user_menu(String UID)
    {
        JFrame primaryFrame=new JFrame("User");
//      primaryFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //view movies
        JButton view_but= new JButton("Movie Catalog");
        view_but.setBounds(20,20,120,25);
        view_but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //movies in db
                JFrame primaryFrame= new JFrame("Movies Available");

                Connection connection = connect();

                //get data from movies

                try{
                    Statement stmt= connection.createStatement();
                    stmt.executeUpdate("USE moviedb");

                    stmt=connection.createStatement();
                    String sql = "SELECT * FROM MOVIES";
                    ResultSet rs = stmt.executeQuery(sql);

                    JTable movie_list = new JTable();

                    movie_list.setModel(DbUtils.resultSetToTableModel(rs));
                    //allow scrolling
                    JScrollPane scrollPane= new JScrollPane(movie_list);



                    primaryFrame.add(scrollPane);
                    primaryFrame.setSize(800,500);
                    primaryFrame.setVisible(true);
                    primaryFrame.setLocationRelativeTo(null);
                    primaryFrame.setResizable(false);
                }
                catch (SQLException e1) {
                    JOptionPane.showMessageDialog(null, e1);
                }
            }
        });

        //view my movies
        JButton my_movie= new JButton("My Movies");
        my_movie.setBounds(150,20,120,25);
        my_movie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFrame primaryFrame= new JFrame("My Movies");
                int UID_int=Integer.parseInt(UID);

                Connection connection= connect();
                //ISSUED.(DOT)==TABLE
                String sql= "SELECT DISTICT ISSUED.*, MOVIES.MNAME, MOVIES.GENRE,MOVIES.PRICE FROM ISSUED, MOVIES "+"WHERE ((ISSUED.UID=" + UID_int +") AND (MOVIES.MID IN (SELECT MID FROM ISSUED.UID="+UID_int+"))) GROUP BY IID";
                //never used sql1
//                String sql1= "SELECT mid FROM ISSUED WHERE UID="+ UID_int;
                try
                {
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate("USE moviedb");
                    stmt= connection.createStatement();
                    //never used arraylist
//                    ArrayList movies_list= new ArrayList();

                    ResultSet rs= stmt.executeQuery(sql);

                    JTable movie_list= new JTable();
                    movie_list.setModel(DbUtils.resultSetToTableModel(rs));
                    JScrollPane scrollPane= new JScrollPane(movie_list);


                    primaryFrame.add(scrollPane);
                    primaryFrame.setSize(800,500);
                    primaryFrame.setVisible(true);
                    primaryFrame.setLocationRelativeTo(null);
                    primaryFrame.setResizable(false);
                }
                catch (SQLException e1)
                {
                    JOptionPane.showMessageDialog(null, e1);
                }

            }
        });

        primaryFrame.add(my_movie);
        primaryFrame.add(view_but);
        primaryFrame.setSize(400,500);
        primaryFrame.setLayout(null);
        primaryFrame.setVisible(true);
        primaryFrame.setLocationRelativeTo(null);
        primaryFrame.setResizable(false);
    }

    //show details of users, movies, issued movies, add movies, return movies, add user, and create or reset the database.
    public static void admin_menu()
    {
        JFrame primaryFrame= new JFrame("Admin");

        //create/reset db
        JButton create_but= new JButton("Create/Reset");
        create_but.setBounds(450,60,120,25);
        create_but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                create();

                JOptionPane.showMessageDialog(null,"Datbase Created/Reset");
            }
        });

        //view movies
        JButton view_but= new JButton("View movies");
        view_but.setBounds(20,20,120,25);
        view_but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame primaryFrame=new JFrame("movies Available");

                Connection connection=connect();
                String sql="SELECT * FROM movies";
                try{
                    Statement stmt= connection.createStatement();
                    stmt.executeUpdate("USE moviedb");
                    stmt= connection.createStatement();

                    ResultSet rs=stmt.executeQuery(sql);

                    JTable movie_list= new JTable();
                    movie_list.setModel(DbUtils.resultSetToTableModel(rs));

                    JScrollPane scrollPane= new JScrollPane(movie_list);

                    primaryFrame.add(scrollPane);
                    primaryFrame.setSize(800,400);
                    primaryFrame.setVisible(true);
                    primaryFrame.setLocationRelativeTo(null);
                    primaryFrame.setResizable(false);
                }
                catch(SQLException e1)
                {
                    JOptionPane.showMessageDialog(null,e1);
                }
            }
        });

        //View Users
        JButton users_but = new JButton("View Users");
        users_but.setBounds(150,20,120,25);
        users_but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame primaryFrame=new JFrame("Users List");

                Connection connection = connect();
                String sql="SELECT * FROM USERS";
                try
                {
                    Statement stmt= connection.createStatement();
                    stmt.executeUpdate("USE moviedb");

                    stmt=connection.createStatement();
                    ResultSet rs= stmt.executeQuery(sql);

                    JTable movie_list= new JTable();
                    movie_list.setModel(DbUtils.resultSetToTableModel(rs));
                    JScrollPane scrollPane=new JScrollPane(movie_list);

                    primaryFrame.add(scrollPane);
                    primaryFrame.setSize(800,400);
                    primaryFrame.setVisible(true);
                    primaryFrame.setLocationRelativeTo(null);
                    primaryFrame.setResizable(false);
                }
                catch(SQLException e1)
                {
                    JOptionPane.showMessageDialog(null,e1);
                }
            }
        });

        //view Issued movies
        JButton issued_but=new JButton("View Issued movies");
        issued_but.setBounds(280,20,160,25);
        issued_but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame primaryFrame=new JFrame("Users List");

                Connection connection=connect();
                String sql="SELECT * FROM ISSUED";

                try
                {
                    Statement stmt= connection.createStatement();
                    stmt.executeUpdate("USE moviedb");

                    stmt=connection.createStatement();
                    ResultSet rs= stmt.executeQuery(sql);
                    JTable movie_list= new JTable();
                    movie_list.setModel(DbUtils.resultSetToTableModel(rs));

                    JScrollPane scrollPane = new JScrollPane(movie_list);

                    primaryFrame.add(scrollPane);
                    primaryFrame.setSize(800, 400);
                    primaryFrame.setVisible(true);
                    primaryFrame.setLocationRelativeTo(null);
                    primaryFrame.setResizable(false);
                }
                catch(SQLException e1)
                {
                    JOptionPane.showMessageDialog(null,e1);
                }
            }
        });

        //add User
        JButton add_user= new JButton("Add User");
        add_user.setBounds(20,60,120,25);
        add_user.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame secondaryFrame= new JFrame("Enter User Details");

                //labels
                JLabel l1= new JLabel("Username");
                JLabel l2= new JLabel("Password");

                l1.setBounds(30,15,100,30);
                l2.setBounds(30,50,100,30);

                //textfield
                JTextField F_user= new JTextField();
                F_user.setBounds(110,15,200,30);

                //passwordfield
                JPasswordField F_pass = new JPasswordField();
                F_pass.setBounds(110,50,200,30);

                //radio button
                //admin
                JRadioButton a1= new JRadioButton("Admin");
                a1.setBounds(55,80,200,30);
                //user
                JRadioButton a2= new JRadioButton("User");
                a1.setBounds(130,80,200,30);

                //add radio buttons
                ButtonGroup bg=new ButtonGroup();
                bg.add(a1);
                bg.add(a2);

                JButton create_but= new JButton("Create");
                create_but.setBounds(130,130,80,25);
                create_but.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {


                        String username = F_user.getText();
                        //change to getpassword for security purpose as getpassword returns array and not string
                        String password = F_pass.getText();
                        Boolean admin = false;

                        if(a1.isSelected()) {
                            admin=true;
                        }

                        Connection connection = connect();

                        try
                        {
                            Statement stmt = connection.createStatement();
                            stmt.executeUpdate("USE moviedb");
                            stmt.executeUpdate("INSERT INTO USERS(USERNAME,PASSWORD,ADMIN) VALUES ('"+username+"','"+password+"',"+admin+")");
                            JOptionPane.showMessageDialog(null,"User added!");
                            secondaryFrame.dispose();
                        }

                        catch (SQLException e1) {
                            JOptionPane.showMessageDialog(null, e1);
                        }
                    }
                });

                secondaryFrame.add(create_but);
                secondaryFrame.add(a2);
                secondaryFrame.add(a1);
                secondaryFrame.add(l1);
                secondaryFrame.add(l2);
                secondaryFrame.add(F_user);
                secondaryFrame.add(F_pass);
                secondaryFrame.setSize(400,500);
                secondaryFrame.setLayout(null);
                secondaryFrame.setVisible(true);
                secondaryFrame.setLocationRelativeTo(null);
                secondaryFrame.setResizable(false);
            }
        });

        //add movie
        JButton add_movie=new JButton("Add movie");

        add_movie.setBounds(150,60,120,25);
        add_movie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFrame secondaryFrame=new JFrame("Enter movie Details");

                JLabel l1,l2,l3;

                l1=new JLabel("movie Name");
                l1.setBounds(30,15, 100,30);


                l2=new JLabel("Genre");
                l2.setBounds(30,53, 100,30);

                l3=new JLabel("Price");
                l3.setBounds(30,90, 100,30);

                JTextField F_mname = new JTextField();
                F_mname.setBounds(110, 15, 200, 30);

                JTextField F_genre=new JTextField();
                F_genre.setBounds(110, 53, 200, 30);

                JTextField F_price=new JTextField();
                F_price.setBounds(110, 90, 200, 30);

                JButton create_but=new JButton("Submit");
                create_but.setBounds(130,130,80,25);
                create_but.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String mname = F_mname.getText();
                        String genre = F_genre.getText();
                        String price = F_price.getText();

                        int price_int = Integer.parseInt(price);

                        Connection connection = connect();

                        try {
                            Statement stmt = connection.createStatement();
                            stmt.executeUpdate("USE moviedb");
                            stmt.executeUpdate("INSERT INTO MOVIES(MNAME,GENRE,PRICE) VALUES ('"+mname+"','"+genre+"',"+price_int+")");
                            JOptionPane.showMessageDialog(null,"Movie added!");
                            secondaryFrame.dispose();

                        }

                        catch (SQLException e1) {
                            JOptionPane.showMessageDialog(null, e1);
                        }
                    }
                });


                secondaryFrame.add(l3);
                secondaryFrame.add(create_but);
                secondaryFrame.add(l1);
                secondaryFrame.add(l2);
                secondaryFrame.add(F_mname);
                secondaryFrame.add(F_genre);
                secondaryFrame.add(F_price);

                secondaryFrame.setSize(400,500);
                secondaryFrame.setLayout(null);
                secondaryFrame.setVisible(true);
                secondaryFrame.setLocationRelativeTo(null);
                secondaryFrame.setResizable(false);
            }
        });

        //Issue movie
        JButton issue_movie=new JButton("Issue movie");
        issue_movie.setBounds(450,20,120,25);
        issue_movie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFrame secondaryFrame = new JFrame("Enter Details");

                JLabel l1,l2,l3,l4;

                l1=new JLabel("Movie ID(mid)");
                l1.setBounds(30,15, 100,30);


                l2=new JLabel("User ID(UID)");
                l2.setBounds(30,53, 100,30);

                l3=new JLabel("Period(days)");
                l3.setBounds(30,90, 100,30);

                l4=new JLabel("Issued Date(DD-MM-YYYY)");
                l4.setBounds(30,127, 150,30);

                JTextField F_mid = new JTextField();
                F_mid.setBounds(110, 15, 200, 30);


                JTextField F_uid=new JTextField();
                F_uid.setBounds(110, 53, 200, 30);

                JTextField F_period=new JTextField();
                F_period.setBounds(110, 90, 200, 30);

                JTextField F_issue=new JTextField();
                F_issue.setBounds(180, 130, 130, 30);

                JButton create_but=new JButton("Submit");
                create_but.setBounds(130,170,80,25);
                create_but.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String uid = F_uid.getText();
                        String mid = F_mid.getText();
                        String period = F_period.getText();
                        String issued_date = F_issue.getText();

                        int period_int = Integer.parseInt(period);

                        Connection connection = connect();

                        try {
                            Statement stmt = connection.createStatement();
                            stmt.executeUpdate("USE moviedb");
                            stmt.executeUpdate("INSERT INTO ISSUED(UID,MID,ISSUED_DATE,PERIOD) VALUES ('"+uid+"','"+mid+"','"+issued_date+"',"+period_int+")");
                            JOptionPane.showMessageDialog(null,"movie Issued!");
                            secondaryFrame.dispose();

                        }
                        catch (SQLException e1) {
                            JOptionPane.showMessageDialog(null, e1);
                        }
                    }
                });

                secondaryFrame.add(l3);
                secondaryFrame.add(l4);
                secondaryFrame.add(create_but);
                secondaryFrame.add(l1);
                secondaryFrame.add(l2);
                secondaryFrame.add(F_uid);
                secondaryFrame.add(F_mid);
                secondaryFrame.add(F_period);
                secondaryFrame.add(F_issue);
                secondaryFrame.setSize(350,250);
                secondaryFrame.setLayout(null);
                secondaryFrame.setVisible(true);
                secondaryFrame.setLocationRelativeTo(null);
                secondaryFrame.setResizable(false);
            }
        });

        //Return movie
        JButton return_movie= new JButton("Return movie");
        return_movie.setBounds(280,60,160,25);
        return_movie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame secondaryFrame=new JFrame("Enter Details");

                //l2,l3 not used
                JLabel l1,l2,l3,l4;

                l1=new JLabel("Issue ID(IID)");
                l1.setBounds(30,15, 100,30);


                l4=new JLabel("Return Date(DD-MM-YYYY)");
                l4.setBounds(30,50, 150,30);

                JTextField F_iid = new JTextField();
                F_iid.setBounds(110, 15, 200, 30);

                JTextField F_return=new JTextField();
                F_return.setBounds(180, 50, 130, 30);

                JButton create_but=new JButton("Return");
                create_but.setBounds(130,170,80,25);
                create_but.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        String iid = F_iid.getText();
                        String return_date = F_return.getText();

                        Connection connection = connect();

                        try {
                            Statement stmt = connection.createStatement();
                            stmt.executeUpdate("USE moviedb");
                            //Initialize date1(issue data) with NULL value
                            String date1 = null;
                            String date2 = return_date; //Initialize date2 with return date

                            //get issue date
                            ResultSet rs = stmt.executeQuery("SELECT ISSUED_DATE FROM ISSUED WHERE IID=" + iid);
                            while (rs.next()) {
                                date1 = rs.getString(1);
                            }


                            try {
                                Date date_1 = new SimpleDateFormat("dd-MM-yyyy").parse(date1);
                                Date date_2 = new SimpleDateFormat("dd-MM-yyyy").parse(date2);
                                //subtract the dates and store in diff
                                long diff = date_2.getTime() - date_1.getTime();
                                //Convert diff from milliseconds to days
                                ex.days = (int) (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
                            }
                            //Parsing Exception
                            catch (ParseException e1) {
                                e1.printStackTrace();
                            }

                            //update return date
                            stmt.executeUpdate("UPDATE ISSUED SET RETURN_DATE='" + return_date + "' WHERE IID=" + iid);
                            secondaryFrame.dispose();

                            Connection connection1 = connect();

                            Statement stmt1 = connection1.createStatement();
                            stmt1.executeUpdate("USE moviedb");

                            //get period from issued table
                            ResultSet rs1 = stmt1.executeQuery("SELECT PERIOD FROM ISSUED WHERE IID=" + iid);
                            String diff = null;
                            while (rs1.next()) {
                                diff = rs1.getString(1);
                            }

                            int diff_int = Integer.parseInt(diff);

                            //If number of days are more than the period then calculate fine
                            if (ex.days > diff_int)
                            {
                                int fine = (ex.days - diff_int) * 10; //fine for every day after the period is Rs 10.

                                stmt1.executeUpdate("UPDATE ISSUED SET FINE=" + fine + " WHERE IID=" + iid);
                                String fine_str = ("Fine: Rs. " + fine);

                                JOptionPane.showMessageDialog(null, fine_str);
                            }

                            JOptionPane.showMessageDialog(null, "Movie Returned!");

                        }
                        catch (SQLException e1)
                        {
                            JOptionPane.showMessageDialog(null, e1);
                        }
                    }

                });

                secondaryFrame.add(l4);
                secondaryFrame.add(create_but);
                secondaryFrame.add(l1);
                secondaryFrame.add(F_iid);
                secondaryFrame.add(F_return);
                secondaryFrame.setSize(350,250);
                secondaryFrame.setLayout(null);
                secondaryFrame.setVisible(true);
                secondaryFrame.setLocationRelativeTo(null);
                secondaryFrame.setResizable(false);

            }
        });

        primaryFrame.add(create_but);
        primaryFrame.add(return_movie);
        primaryFrame.add(issue_movie);
        primaryFrame.add(add_movie);
        primaryFrame.add(issued_but);
        primaryFrame.add(users_but);
        primaryFrame.add(view_but);
        primaryFrame.add(add_user);
        primaryFrame.setSize(600,200);
        primaryFrame.setLayout(null);
        primaryFrame.setVisible(true);
        primaryFrame.setLocationRelativeTo(null);
        primaryFrame.setResizable(false);
    }

}
