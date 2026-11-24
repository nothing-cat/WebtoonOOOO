import {useState} from 'react'
import SearchBar from './SearchBar.jsx'
import {useAuth} from '../context/AuthContext.jsx'
import {useData} from '../hooks/useData.js'
import {go} from '../router.js'
import {mockMode,query} from '../services/api.js'
export default function Layout({children,path,params}){
 const [keyword,setKeyword]=useState('')
 const {user,ready}=useAuth()
 const sidebar=path==='/'||path==='/explore'
 const {data:genres}=useData(sidebar?'/genres':null,[])
 return <div className="site-frame">
 <header><div className="header-inner">
 <a className="brand" href="#/">무드툰</a>
 <SearchBar keyword={keyword} onKeywordChange={setKeyword} onEnter={()=>go('/explore'+query({keyword:keyword.trim()}))}/>
 <nav aria-label="계정 메뉴">{ready&&(user?<a className="profile-circle" href="#/profile" aria-label="내 프로필">프로필</a>:<><a href="#/login">로그인</a><a href="#/signup">회원가입</a></>)}</nav>
 </div></header>
 <div className={'shell'+(sidebar?' with-sidebar':'')}>
 {sidebar&&<aside><h3>인기 웹툰</h3>{(genres||[]).map(g=><a className={params.get('genre')===String(g.id)&&params.get('popular')==='true'?'selected':''} key={g.id} href={'#/explore'+query({genre:g.id,popular:'true'})}>{g.name}</a>)}<hr/><h3><a href="#/board">게시판</a></h3>{[['GENERAL','일반'],['REVIEW','리뷰'],['QUESTION','질문']].map(([k,v])=><a key={k} href={'#/board?category='+k}>{v}</a>)}</aside>}
 <main>{children}</main></div>
 {mockMode&&<footer className="demo-note">데모 모드 · 가상 데이터 · 실제 비밀번호를 입력하지 마세요.</footer>}
 </div>
}
