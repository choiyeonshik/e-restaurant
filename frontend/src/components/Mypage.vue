<template>

<v-data-table
    :headers="headers"
    :items="mypage"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'Mypage',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "employeeCardNo", value: "employeeCardNo" },
            { text: "menuname", value: "menuname" },
            { text: "orderid", value: "orderid" },
            { text: "cookid", value: "cookid" },
            { text: "cookingstatus", value: "cookingstatus" },
            { text: "paymentstatus", value: "paymentstatus" },
            { text: "amount", value: "amount" },
            { text: "paiddate", value: "paiddate" },
            { text: "paymentid", value: "paymentid" },
        ],
        mypage : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/mypages')

      this.mypage = temp.data._embedded.mypages;

    },
    methods: {
    }
  }
</script>

