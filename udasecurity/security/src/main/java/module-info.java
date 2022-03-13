module com.udacity.udasecurity.security {
    requires com.udacity.udasecurity.image;
    requires java.desktop;
    requires java.prefs;
    requires com.google.gson;
    requires java.sql;
    requires guava;
    requires miglayout.swing;
    opens com.udacity.udasecurity.security.data to com.google.gson;
}