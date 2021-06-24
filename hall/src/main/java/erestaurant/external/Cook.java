package erestaurant.external;

import java.util.Date;

public class Cook {

    private Long cookid;
    private Long orderid;
    private Date cookeddate;
    private String status;
    private String menuname;

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
    public Date getCookeddate() {
        return cookeddate;
    }
    public void setCookeddate(Date cookeddate) {
        this.cookeddate = cookeddate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getMenuname() {
        return menuname;
    }

    public void setMenuname(String menuname) {
        this.menuname = menuname;
    }
}
