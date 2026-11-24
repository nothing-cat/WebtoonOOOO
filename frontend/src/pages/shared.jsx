import {useState} from 'react'
import {useAuth} from '../context/AuthContext.jsx'
import {useData} from '../hooks/useData.js'
import {request} from '../services/api.js'
import {categories} from '../mocks/data.js'
export function Status({state,children}){if(state.loading)return <p role="status">불러오는 중…</p>;if(state.error)return <p role="alert" className="error">{state.error}</p>;return children}
export function PostList({posts=[],compact=false}){return posts.length?<div className={'post-list'+(compact?' compact':'')}>{posts.map(p=><a className="post-row" key={p.id} href={'#/board/'+p.id}><strong>{p.title}</strong><small>{categories[p.category]||p.category} · {p.username} · {p.category==='NOTICE'?(p.createdAt||'').slice(0,10).replaceAll('-','.'):('좋아요 '+(p.likeCount||0))}</small></a>)}</div>:<p className="muted">게시글이 없습니다.</p>}
export function Comments({path,webtoon=false,onChange}){
 const {user}=useAuth();const [version,refresh]=useState(0),[value,setValue]=useState(''),[editing,setEditing]=useState(null),[error,setError]=useState('')
 const list=useData(path,[],version);const field=webtoon?'text':'content';const itemPath=id=>(webtoon?'/comments/':'/post-comments/')+id
 async function act(p,m,b){try{setError('');await request(p,m,b);setValue('');setEditing(null);refresh(x=>x+1);onChange?.()}catch(e){setError(e.message)}}
 return <>
 {user?<form className="comment-form" onSubmit={e=>{e.preventDefault();act(editing?itemPath(editing):path,editing?'PUT':'POST',{[field]:value})}}><textarea aria-label="댓글" required value={value} onChange={e=>setValue(e.target.value)} placeholder="댓글을 입력해 주세요."/><div className="comment-form-actions">{editing&&<button className="secondary" type="button" onClick={()=>{setEditing(null);setValue('')}}>취소</button>}<button className="secondary">{editing?'수정 완료':'등록'}</button></div></form>:<p className="muted">댓글 작성은 <a href="#/login">로그인</a> 후 가능합니다.</p>}
 {error&&<p className="error" role="alert">{error}</p>}
 <Status state={list}>{!(list.data||[]).length&&<p className="muted">아직 댓글이 없습니다.</p>}{(list.data||[]).map(c=><article className="comment" key={c.id}><div className="comment-head"><strong>{c.username}</strong><div className="text-actions">{webtoon&&<button disabled={!user} onClick={()=>act(itemPath(c.id)+'/like',c.isLiked?'DELETE':'POST')}>{c.isLiked?'♥':'♡'} {c.empathyCount||0}</button>}{user?.id===c.userId&&<button onClick={()=>{setEditing(c.id);setValue(c[field])}}>수정</button>}{user&&(user.id===c.userId||user.role==='ADMIN')&&<button onClick={()=>{if(confirm('댓글을 삭제할까요?'))act(itemPath(c.id),'DELETE')}}>삭제</button>}</div></div><p>{c[field]}</p></article>)}</Status></>
}
