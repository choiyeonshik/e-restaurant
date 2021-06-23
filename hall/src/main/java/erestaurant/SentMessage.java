package erestaurant;

public class SentMessage extends AbstractEvent {

    private Long orderid;

    public SentMessage(){
        super();
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
}
