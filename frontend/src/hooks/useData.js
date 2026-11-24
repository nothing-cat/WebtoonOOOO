import {useEffect,useState} from 'react'
export function useData(load,key,initial=null,version=0){
 const [state,setState]=useState({key:null,version:null,data:initial,error:'',loading:true})
 useEffect(()=>{let active=true;if(key===null)return;Promise.resolve().then(load).then(data=>{if(active)setState({key,version,data,error:'',loading:false})}).catch(e=>{if(active)setState({key,version,data:null,error:e.message,loading:false})});return()=>{active=false}},[key,version]) // key identifies request arguments
 return key===null?{data:initial,error:'',loading:false}:state.key===key&&state.version===version?state:{data:initial,error:'',loading:true}
}
