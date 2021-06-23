
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import OrderManager from "./components/OrderManager"

import CookManager from "./components/CookManager"


import Mypage from "./components/Mypage"
import PaymentManager from "./components/PaymentManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/cooks',
                name: 'CookManager',
                component: CookManager
            },


            {
                path: '/mypages',
                name: 'Mypage',
                component: Mypage
            },
            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
            },



    ]
})
