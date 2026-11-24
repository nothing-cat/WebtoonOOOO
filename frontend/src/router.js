import {useEffect,useState} from 'react'
export const go=path=>{window.location.hash=path}
export function useRoute(){const [route,setRoute]=useState(()=>location.hash.slice(1)||'/');useEffect(()=>{const update=()=>{setRoute(location.hash.slice(1)||'/');window.scrollTo(0,0)};window.addEventListener('hashchange',update);return()=>window.removeEventListener('hashchange',update)},[]);const [path,search='']=route.split('?');return {path,params:new URLSearchParams(search),route}}
