package thread;

class Ticket {
    //票数
    private int num=100;
    //卖票
    public synchronized void saleTicket(){
       if (num>0){
           System.out.println(Thread.currentThread().getName()+"卖了"+num+"票");
           num--;
       }
    }
}

 class SaleTicket{
    //创建多个线程
    public static void main(String[] args) {
        Ticket ticket = new Ticket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <100 ; i++) {
                    ticket.saleTicket();
                }
            }
        },"A线程").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100 ; i++) {
                    ticket.saleTicket();
                }
            }
        },"B线程").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <100 ; i++) {
                    ticket.saleTicket();
                }
            }
        },"C线程").start();
    }

}
