package erestaurant;

public class Receieved extends AbstractEvent {

    private Long cookid;
    private Long orderid;
    private String status;

    public Receieved(){
        super();
    }

    public Long getCookid() {
        return cookid;
    }

    public void setCookid(Long cookid) {
        this.cookid = cookid;
    }
    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
