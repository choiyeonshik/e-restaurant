package erestaurant;

import java.util.Date;

public class Cooked extends AbstractEvent {

    private Long cookid;
    private Date cookeddate;
    private Long orderid;
    private String status;

    public Cooked(){
        super();
    }

    public Long getCookid() {
        return cookid;
    }

    public void setCookid(Long cookid) {
        this.cookid = cookid;
    }
    public Date getCookeddate() {
        return cookeddate;
    }

    public void setCookeddate(Date cookeddate) {
        this.cookeddate = cookeddate;
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
