package erestaurant;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.Date;

@Entity
@Table(name="Cook_table")
public class Cook {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long cookid;
    private Long orderid;
    private Date cookeddate;
    private String status;
    private String menuname;

    @PostPersist
    public void onPostPersist(){
        Receieved receieved = new Receieved();
        BeanUtils.copyProperties(this, receieved);
        receieved.publishAfterCommit();

    }
    @PostUpdate
    public void onPostUpdate(){
        Cooked cooked = new Cooked();
        BeanUtils.copyProperties(this, cooked);
        cooked.publishAfterCommit();

    }
    @PreUpdate
    public void onPreUpdate(){
        if ("요리완료".equals(this.status)) {
            this.cookeddate = new Date(System.currentTimeMillis());
        } else if ("접수완료".equals(this.status)) {
            this.cookeddate = new Date(System.currentTimeMillis());
            this.status = "요리완료";
        } else {
            this.status = "접수완료";
        }
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
