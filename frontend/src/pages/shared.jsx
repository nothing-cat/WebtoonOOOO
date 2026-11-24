import {useState} from 'react'
import {useAuth} from '../context/AuthContext.jsx'
import {useData} from '../hooks/useData.js'
import * as api from '../services/postCommentApi.js'
import {categories} from '../constants/categories.js'
export function Status({state,children}){if(state.loading)return <p role="status">불러오는 중…</p>;if(state.error)return <p role="alert" className="error">{state.error}</p>;return children}
export function PostList({posts=[],compact=false}){return posts.length?<div className={'post-list'+(compact?' compact':'')}>{posts.map(p=><a className="post-row" key={p.id} href={'#/board/'+p.id}><strong>{p.title}</strong><small>{categories[p.category]||p.category} · {p.username} · {p.category==='NOTICE'?(p.createdAt||'').slice(0,10).replaceAll('-','.'):('좋아요 '+(p.likeCount||0))}</small></a>)}</div>:<p className="muted">게시글이 없습니다.</p>}
export function Comments({postId,onChange}){
 const {currentUser}=useAuth()
 const [version,refresh]=useState(0),[value,setValue]=useState(''),[editing,setEditing]=useState(null),[error,setError]=useState(''),[busy,setBusy]=useState(false)
 const list=useData(()=>api.fetchPostComments(postId),'post-comments:'+postId,[],version)
 async function act(fn){if(busy)return;setBusy(true);try{setError('');await fn();setValue('');setEditing(null);refresh(x=>x+1);onChange?.()}catch(e){setError(e.message)}finally{setBusy(false)}}
 return <>{currentUser?<form className="board-comment-form" onSubmit={e=>{e.preventDefault();if(value.trim())act(()=>editing?api.updatePostComment(editing,value.trim()):api.createPostComment(postId,value.trim()))}}><textarea aria-label="댓글" required value={value} onChange={e=>setValue(e.target.value)} placeholder="댓글을 입력해 주세요."/><div className="comment-form-actions">{editing&&<button className="secondary" type="button" onClick={()=>{setEditing(null);setValue('')}}>취소</button>}<button disabled={busy} className="secondary">{editing?'수정 완료':'등록'}</button></div></form>:<p className="muted">댓글 작성은 <a href="#/login">로그인</a> 후 가능합니다.</p>}
 {error&&<p className="error" role="alert">{error}</p>}
 <Status state={list}>{!(list.data||[]).length&&<p className="muted">아직 댓글이 없습니다.</p>}{(list.data||[]).map(c=><article className="board-comment" key={c.id}><div className="board-comment-head"><strong>{c.username}</strong><div className="text-actions">{currentUser?.id===c.userId&&<button disabled={busy} onClick={()=>{setEditing(c.id);setValue(c.content)}}>수정</button>}{currentUser&&(currentUser.id===c.userId||currentUser.role==='ADMIN')&&<button disabled={busy} onClick={()=>{if(confirm('댓글을 삭제할까요?'))act(()=>api.deletePostComment(c.id))}}>삭제</button>}</div></div><p>{c.content}</p></article>)}</Status></>
}
