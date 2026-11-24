import {useState} from 'react'
import SearchBar from './SearchBar.jsx'
import {useAuth} from '../context/AuthContext.jsx'
import {useData} from '../hooks/useData.js'
import {go} from '../router.js'
import {mockMode,query} from '../services/api.js'
import {categories} from '../mocks/data.js'
export default function Layout({children}){const [keyword,setKeyword]=useState(''),[genre,setGenre]=useState('');const {user,logout}=useAuth();const {data:genres}=useData('/genres',[]);const {data:popular}=useData('/webtoons/popular'+query({genre}),[])
 return <><header><div className="header-inner"><a className="brand" href="#/">웹툰모아</a><SearchBar keyword={keyword} onKeywordChange={setKeyword} onEnter={()=>go('/explore'+query({keyword}))}/><nav><a href="#/explore">웹툰 탐색</a><a href="#/board">게시판</a>{user?<><a href="#/profile">{user.username} 님</a><button className="quiet" onClick={()=>logout().then(()=>go('/')).catch(e=>alert(e.message))}>로그아웃</button></>:<><a href="#/login">로그인</a><a href="#/signup">회원가입</a></>}</nav></div></header>{mockMode&&<div className="demo">데모 모드 · 가상 데이터 / 실제 비밀번호를 입력하지 마세요.</div>}<div className="shell"><aside><h3>인기 웹툰</h3><select aria-label="인기 장르" value={genre} onChange={e=>setGenre(e.target.value)}><option value="">전체 장르</option>{(genres||[]).map(g=><option key={g.id} value={g.id}>{g.name}</option>)}</select><ol>{(popular||[]).slice(0,5).map(w=><li key={w.id}><a href={'#/webtoon/'+encodeURIComponent(w.id)}>{w.title}</a></li>)}</ol><h3>게시판</h3><a href="#/board">전체 게시글</a>{Object.entries(categories).map(([k,v])=><a key={k} href={'#/board?category='+k}>{v}</a>)}</aside><main>{children}</main></div><footer>웹툰모아 · 웹툰 정보와 이야기를 한곳에서</footer></>
}
