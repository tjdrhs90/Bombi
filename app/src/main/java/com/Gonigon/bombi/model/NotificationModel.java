package com.Gonigon.bombi.model;


public class NotificationModel {

    public String to;

    public Notification notification = new Notification();
    public Data data = new Data();

//    public static class Notification {
//        public String title;
//        public String body;
//
//    }
//    public static  class Data{
//        public String title;
//        public String body;
//    }

    public class Notification {
        public String title;
        public String body;

    }
    public class Data {
        public String title;
        public String body;
    }



}
