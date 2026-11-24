import {useState,useEffect} from 'react'
import * as api from '../services/commentApi.js'
import {useAuth} from '../context/AuthContext.jsx'
export function useComments(webtoonId){
 const {currentUser}=useAuth()
 const [commentList,setList]=useState([]),[isLoading,setLoading]=useState(true),[version,refresh]=useState(0)
 useEffect(()=>{let active=true;setLoading(true);api.fetchComments(webtoonId).then(rows=>{if(active)setList(rows)}).catch(e=>{if(active)alert(e.message)}).finally(()=>{if(active)setLoading(false)});return()=>{active=false}},[webtoonId,version,currentUser?.id])
 async function act(fn){if(!currentUser){alert('로그인 후 이용해 주세요.');return false}try{await fn();refresh(v=>v+1);return true}catch(e){alert(e.message);return false}}
 return {commentList,isLoading,addComment:text=>act(()=>api.createComment(webtoonId,text)),deleteComment:id=>act(()=>api.deleteComment(id)),editComment:(id,text)=>act(()=>api.updateComment(id,text)),toggleEmpathy:id=>act(()=>api.setCommentLike(id,!commentList.find(c=>c.id===id)?.isLiked))}
}
