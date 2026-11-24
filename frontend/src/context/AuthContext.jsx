import {createContext,useContext,useEffect,useState} from 'react'
import {request} from '../services/api.js'
const AuthContext=createContext(null)
export function AuthProvider({children}){const [currentUser,setCurrentUser]=useState(null),[isLoading,setLoading]=useState(true)
useEffect(()=>{let active=true;request('/users/me').then(u=>{if(active)setCurrentUser(u)}).catch(()=>{}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[])
const login=async form=>{try{await request('/auth/login','POST',form);setCurrentUser(await request('/users/me'));return {ok:true}}catch(e){return {ok:false,message:e.message}}}
const signup=async form=>{try{await request('/auth/signup','POST',form);return {ok:true}}catch(e){return {ok:false,message:e.message}}}
const logout=async()=>{await request('/auth/logout','POST');setCurrentUser(null)}
return <AuthContext.Provider value={{currentUser,isLoading,user:currentUser,ready:!isLoading,login,signup,logout}}>{children}</AuthContext.Provider>}
export const useAuth=()=>useContext(AuthContext)
