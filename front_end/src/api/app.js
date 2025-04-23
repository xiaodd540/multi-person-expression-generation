import axios from 'axios';
import { useRouter } from 'vue-router';
import { defineStore } from 'pinia'
import {reactive, ref, toRefs} from 'vue'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const api = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
});

const getToken = () => localStorage.getItem('token');
const getRefreshToken = () => localStorage.getItem('refreshToken');
const setToken = (token) => localStorage.setItem('token', token);
const removeTokens = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
};

const router = useRouter();

// 请求拦截器
api.interceptors.request.use(
    (config) => {
        const token = getToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 响应拦截器
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            const refreshToken = getRefreshToken();
            if (!refreshToken) {
                removeTokens();
                await router.push('/login');
                return Promise.reject(error);
            }

            try {
                const res = await axios.post('http://localhost:8080/api/refresh', {}, {
                    headers: { Authorization: `Bearer ${refreshToken}` }
                });
                if (res.data.token) {
                    setToken(res.data.token);
                    return api.request(error.config); // 重新发起失败的请求
                }
            } catch (refreshError) {
                removeTokens();
                await router.push('/login');
            }
        }
        return Promise.reject(error);
    }
);

//修改头像进行同步的
export const useUserStore = defineStore('user', () => {
    const avatar = ref('/vite.svg') // 默认头像
    const username = ref('')
    const userId=ref(null)

    function setAvatar(newAvatar) {
        avatar.value = newAvatar
    }

    function setUsername(newUsername) {
        username.value = newUsername
    }

    function setId(newId) {
        userId.value = Number(newId);
    }

    return { avatar, username , userId, setAvatar , setUsername , setId};
},{
    persist: {
        key: 'user', // 存储在 localStorage 的 key
        storage: localStorage, // 也可以改成 sessionStorage
    }
})

export default api;

