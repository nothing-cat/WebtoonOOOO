import {useEffect,useState} from 'react'
import {request} from '../services/api.js'
export function useData(path,initial=null,version=0){
 const [state,setState]=useState({path:null,data:initial,error:'',loading:true})
 useEffect(()=>{let active=true;if(!path)return;request(path).then(data=>{if(active)setState({path,data,error:'',loading:false})}).catch(e=>{if(active)setState({path,data:null,error:e.message,loading:false})});return()=>{active=false}},[path,version])
 return !path?{data:initial,error:'',loading:false}:state.path===path?state:{data:initial,error:'',loading:true}
}
