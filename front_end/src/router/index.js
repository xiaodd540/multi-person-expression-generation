import { createRouter, createWebHistory } from 'vue-router'
import index from '../views/index.vue'
import auto from "../components/auto.vue";
import manual from "../components/manual.vue";
import login from "../views/login.vue";
import register from "../views/register.vue";
import owner from "../components/owner.vue";
import resource from "../components/resource.vue";
import HP_generator from "../components/HP_generator.vue";
import about from "../components/about.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'home',
            component: index,
        },
        {
            path: '/auto',
            name: 'auto',
            component: auto,
        },
        {
            path: '/manual',
            name: 'manual',
            component: manual,
        },
        {
            path: '/login',
            name: 'login',
            component: login,
        },
        {
            path: '/register',
            name: 'register',
            component: register,
        },
        {
            path: '/owner',
            name: 'owner',
            component: owner,
        },
        {
            path: '/resource',
            name: 'resource',
            component: resource,
        },
        {
            path: '/HP-generator',
            name: 'hp-generator',
            component:HP_generator,
        },
        {
            path: '/about',
            name: 'about',
            component: about,
        }
    ],
})

export default router