package com;


public class WorkThread extends Thread {
    class ShutdownCallbackThread extends Thread {
        public void run() {
            System.out.println("程序被关闭");
            flag = false;
        }//设置关闭筏值
    }

    private boolean flag = true;

    public void run() {
        //regist hook
        ShutdownCallbackThread hook = new ShutdownCallbackThread();
        Runtime.getRuntime().addShutdownHook(hook);
        while (flag) {
            System.out.println("working");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        new WorkThread().start();
    }
}
