import {useState} from 'react'
import {useAuth} from '../context/AuthContext.jsx'
import {request,mockMode} from '../services/api.js'
import {go} from '../router.js'
import {useData} from '../hooks/useData.js'
import WebtoonGrid from '../components/WebtoonGrid.jsx'
import {PostList,Status} from './shared.jsx'
export function Account({signup=false}){const [username,setUsername]=useState(''),[password,setPassword]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false);const {login}=useAuth()
async function submit(e){e.preventDefault();setBusy(true);setError('');try{if(signup){await request('/auth/signup','POST',{username,password});go('/login')}else{await login({username,password});go('/')}}catch(e){setError(e.message)}finally{setBusy(false)}}
return <section className="account panel"><h1>{signup?'회원가입':'로그인'}</h1>{mockMode&&<p className="muted">데모 로그인: demo 또는 admin / 임의의 테스트 비밀번호. 실제 인증이 아닙니다.</p>}<form onSubmit={submit}><label>아이디<input required pattern="[a-z0-9_]{4,20}" value={username} onChange={e=>setUsername(e.target.value)} autoComplete="username"/></label><label>비밀번호<input required type="password" value={password} onChange={e=>setPassword(e.target.value)} autoComplete={signup?'new-password':'current-password'}/></label>{error&&<p className="error">{error}</p>}<button disabled={busy}>{signup?'회원가입':'로그인'}</button></form><a href={signup?'#/login':'#/signup'}>{signup?'로그인':'회원가입'}</a></section>}
export function Profile(){const {user,ready}=useAuth();const favorites=useData(user?'/users/me/favorites':null,[]),posts=useData(user?'/users/me/posts':null,[]),comments=useData(user?'/users/me/comments':null,[]),postComments=useData(user?'/users/me/post-comments':null,[])
if(!ready)return <p>불러오는 중…</p>;if(!user)return <p><a href="#/login">로그인</a>이 필요합니다.</p>
return <><h1>마이페이지</h1><p>{user.username} · {user.role==='ADMIN'?'관리자':'일반 회원'}</p><section><h2>좋아하는 웹툰</h2><Status state={favorites}><WebtoonGrid webtoons={favorites.data||[]}/></Status></section><section><h2>내가 작성한 게시글</h2><Status state={posts}><PostList posts={posts.data||[]}/></Status></section><section><h2>웹툰 댓글</h2><Status state={comments}>{(comments.data||[]).map(c=><p key={c.id}><a href={'#/webtoon/'+encodeURIComponent(c.webtoonId)}>{c.text}</a></p>)}</Status></section><section><h2>게시글 댓글</h2><Status state={postComments}>{(postComments.data||[]).map(c=><p key={c.id}><a href={'#/board/'+c.postId}>{c.content}</a></p>)}</Status></section></>}
