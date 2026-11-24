import {createContext,useContext,useEffect,useState} from 'react'
import * as authApi from '../services/authApi.js'
import {fetchCurrentUser} from '../services/userApi.js'
const AuthContext=createContext(null)
export function AuthProvider({children}){
 const [currentUser,setCurrentUser]=useState(null)
 const [isLoading,setLoading]=useState(true)
 useEffect(()=>{let active=true;fetchCurrentUser().then(u=>{if(active)setCurrentUser(u)}).catch(()=>{}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[])
 const login=async form=>{try{await authApi.login(form);setCurrentUser(await fetchCurrentUser());return {ok:true}}catch(e){return {ok:false,message:e.message}}}
 const signup=async form=>{try{await authApi.signup(form);return {ok:true}}catch(e){return {ok:false,message:e.message}}}
 const logout=async()=>{await authApi.logout();setCurrentUser(null)}
 return <AuthContext.Provider value={{currentUser,isLoading,login,signup,logout}}>{children}</AuthContext.Provider>
}
export const useAuth=()=>useContext(AuthContext)
