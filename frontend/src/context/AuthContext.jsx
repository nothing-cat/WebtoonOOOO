import {createContext,useContext,useEffect,useState} from 'react'
import {request} from '../services/api.js'
const AuthContext=createContext(null)
export function AuthProvider({children}){const [user,setUser]=useState(null);const [ready,setReady]=useState(false)
 useEffect(()=>{let alive=true;request('/users/me').then(u=>{if(alive)setUser(u)}).catch(()=>{}).finally(()=>{if(alive)setReady(true)});return()=>{alive=false}},[])
 const login=async(body)=>{await request('/auth/login','POST',body);setUser(await request('/users/me'))}
 const logout=async()=>{await request('/auth/logout','POST');setUser(null)}
 return <AuthContext.Provider value={{user,ready,login,logout}}>{children}</AuthContext.Provider>
}
export const useAuth=()=>useContext(AuthContext)
