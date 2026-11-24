import {useState} from 'react'
import {useAuth} from '../context/AuthContext.jsx'
import {request,mockMode} from '../services/api.js'
import {go,useRoute} from '../router.js'
import {useData} from '../hooks/useData.js'
import WebtoonGrid from '../components/WebtoonGrid.jsx'
import {PostList,Status} from './shared.jsx'
export function Account({signup=false}){
 const [username,setUsername]=useState(''),[password,setPassword]=useState(''),[show,setShow]=useState(false),[error,setError]=useState(''),[busy,setBusy]=useState(false);const {login}=useAuth()
 async function submit(e){e.preventDefault();setBusy(true);setError('');try{if(signup){await request('/auth/signup','POST',{username,password});go('/login')}else{await login({username,password});go('/')}}catch(e){setError(e.message)}finally{setBusy(false)}}
 const idInput=<input id="account-id" required pattern="[a-z0-9_]{4,20}" placeholder="아이디를 입력하세요." value={username} onChange={e=>setUsername(e.target.value)} autoComplete="username"/>
 const passwordInput=<div className="password-field"><input id="account-password" required type={show?'text':'password'} placeholder="비밀번호를 입력하세요." value={password} onChange={e=>setPassword(e.target.value)} autoComplete={signup?'new-password':'current-password'}/>{signup&&<button type="button" className="password-toggle" aria-label={show?'비밀번호 숨기기':'비밀번호 표시'} onClick={()=>setShow(v=>!v)}><svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" aria-hidden="true"><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z"/><circle cx="12" cy="12" r="2.5"/>{!show&&<path d="m3 3 18 18"/>}</svg></button>}</div>
 return signup?<section className="signup-wrap"><div className="signup-card"><h1>회원가입</h1><form onSubmit={submit}><label htmlFor="account-id">아이디</label>{idInput}<p className="input-hint">영문 소문자, 숫자, 밑줄 4~20자</p><label htmlFor="account-password">비밀번호</label>{passwordInput}{error&&<p className="error" role="alert">{error}</p>}<button className="signup-submit" disabled={busy}>회원가입</button></form><p className="signup-bottom">이미 계정이 있으신가요? <a href="#/login">로그인</a></p></div></section>:<><h1>로그인</h1><section className="login-wrap"><a className="signup-link" href="#/signup">회원가입하러 가기 →</a><form className="login-form" onSubmit={submit}><label htmlFor="account-id">아이디</label>{idInput}<label htmlFor="account-password">비밀번호</label>{passwordInput}{error&&<p className="error login-message">{error}</p>}<div className="login-submit"><button disabled={busy}>로그인</button></div></form>{mockMode&&<p className="input-hint">테스트용 아이디: demo 또는 admin · 임의의 테스트 비밀번호</p>}</section></>
}
export function Profile(){
 const {user,ready,logout}=useAuth();const {params}=useRoute();const tab=params.get('tab')||'all'
 const favorites=useData(user?'/users/me/favorites':null,[]),posts=useData(user?'/users/me/posts':null,[]),comments=useData(user?'/users/me/comments':null,[]),postComments=useData(user?'/users/me/post-comments':null,[])
 if(!ready)return <p>불러오는 중…</p>;if(!user)return <p><a href="#/login">로그인</a>이 필요합니다.</p>
 const tabs=[['all','전체 보기'],['favorites','좋아하는 작품'],['comments','웹툰에 남긴 댓글'],['posts','내가 쓴 게시글'],['post-comments','게시글에 남긴 댓글']]
 const visible=k=>tab==='all'||tab===k
 return <><h1>내 프로필</h1><div className="profile-meta"><p>{user.username}{user.role==='ADMIN'?' · 관리자':''}</p><button className="quiet" onClick={()=>logout().then(()=>go('/')).catch(e=>alert(e.message))}>로그아웃</button></div><div className="profile-tabs">{tabs.map(([k,v])=><a key={k} className={tab===k?'active':''} href={'#/profile?tab='+k}>{v}</a>)}</div>
 {visible('favorites')&&<section>{tab==='all'&&<h2>좋아하는 작품</h2>}<Status state={favorites}><WebtoonGrid webtoons={favorites.data||[]}/></Status></section>}
 {visible('comments')&&<section>{tab==='all'&&<h2>웹툰에 남긴 댓글</h2>}<Status state={comments}>{!(comments.data||[]).length&&<p className="muted">작성한 댓글이 없습니다.</p>}{(comments.data||[]).map(c=><a className="activity-row" key={c.id} href={'#/webtoon/'+encodeURIComponent(c.webtoonId)}>{c.text}</a>)}</Status></section>}
 {visible('posts')&&<section>{tab==='all'&&<h2>내가 쓴 게시글</h2>}<Status state={posts}><PostList posts={posts.data||[]}/></Status></section>}
 {visible('post-comments')&&<section>{tab==='all'&&<h2>게시글에 남긴 댓글</h2>}<Status state={postComments}>{!(postComments.data||[]).length&&<p className="muted">작성한 댓글이 없습니다.</p>}{(postComments.data||[]).map(c=><a className="activity-row" key={c.id} href={'#/board/'+c.postId}>{c.content}</a>)}</Status></section>}</>
}
