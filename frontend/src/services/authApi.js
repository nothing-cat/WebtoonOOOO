import {request} from './apiClient.js'
export const login = form => request('/auth/login','POST',form)
export const signup = form => request('/auth/signup','POST',form)
export const logout = () => request('/auth/logout','POST')
