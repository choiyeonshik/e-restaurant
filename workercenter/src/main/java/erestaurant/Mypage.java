package erestaurant;

import javax.persistence.*;

import java.util.Date;

@Entity
@Table(name="Mypage_table")
public class Mypage {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long employeeCardNo;
        private String menuname;
        private Long orderid;
        private Long cookid;
        private String cookingstatus;
        private String paymentstatus;
        private Long amount;
        private Date paiddate;
        private Long paymentid;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public Long getEmployeeCardNo() {
            return employeeCardNo;
        }

        public void setEmployeeCardNo(Long employeeCardNo) {
            this.employeeCardNo = employeeCardNo;
        }
        public String getMenuname() {
            return menuname;
        }

        public void setMenuname(String menuname) {
            this.menuname = menuname;
        }
        public Long getOrderid() {
            return orderid;
        }

        public void setOrderid(Long orderid) {
            this.orderid = orderid;
        }
        public Long getCookid() {
            return cookid;
        }

        public void setCookid(Long cookid) {
            this.cookid = cookid;
        }
        public String getCookingstatus() {
            return cookingstatus;
        }

        public void setCookingstatus(String cookingstatus) {
            this.cookingstatus = cookingstatus;
        }
        public String getPaymentstatus() {
            return paymentstatus;
        }

        public void setPaymentstatus(String paymentstatus) {
            this.paymentstatus = paymentstatus;
        }
        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }
        public Date getPaiddate() {
            return paiddate;
        }

        public void setPaiddate(Date paiddate) {
            this.paiddate = paiddate;
        }
        public Long getPaymentid() {
            return paymentid;
        }

        public void setPaymentid(Long paymentid) {
            this.paymentid = paymentid;
        }

}
