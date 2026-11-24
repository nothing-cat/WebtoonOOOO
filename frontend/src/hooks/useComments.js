import {useState,useEffect} from 'react'
import {request} from '../services/api.js'
import {useAuth} from '../context/AuthContext.jsx'
export function useComments(webtoonId){const {currentUser}=useAuth();const [commentList,setList]=useState([]),[isLoading,setLoading]=useState(true),[version,refresh]=useState(0);const path='/webtoons/'+encodeURIComponent(webtoonId)+'/comments'
useEffect(()=>{let active=true;request(path).then(rows=>{if(active)setList(rows.map(c=>({...c,user:c.username,empathy:c.empathyCount||0})))}).catch(e=>alert(e.message)).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[path,version,currentUser?.id])
async function act(p,m,b){if(!currentUser){alert('로그인 후 이용해 주세요.');return false}try{await request(p,m,b);refresh(v=>v+1);return true}catch(e){alert(e.message);return false}}
return {commentList,isLoading,addComment:text=>act(path,'POST',{text}),deleteComment:id=>act('/comments/'+id,'DELETE'),editComment:(id,text)=>act('/comments/'+id,'PUT',{text}),toggleEmpathy:id=>act('/comments/'+id+'/like',commentList.find(c=>c.id===id)?.isLiked?'DELETE':'POST')}}
