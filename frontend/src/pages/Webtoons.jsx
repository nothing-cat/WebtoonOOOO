import {useState} from 'react'
import {useData} from '../hooks/useData.js'
import {useAuth} from '../context/AuthContext.jsx'
import {request,query} from '../services/api.js'
import {go} from '../router.js'
import WebtoonGrid from '../components/WebtoonGrid.jsx'
import {PostList,Comments,Status} from './shared.jsx'

export function Home(){
 const {user}=useAuth()
 const favorites=useData(user?'/users/me/favorites':null,[])
 const popular=useData('/webtoons/popular',[])
 const posts=useData('/posts/popular',[])
 return <><h1>홈</h1>
 {user&&<section className="home-section"><h2><a href="#/profile?tab=favorites">좋아하는 웹툰</a></h2><p className="section-hint">최근 클릭한 순</p><Status state={favorites}><WebtoonGrid webtoons={(favorites.data||[]).slice(0,3)}/></Status></section>}
 <section className="home-section"><h2>인기 웹툰</h2><Status state={popular}><WebtoonGrid webtoons={(popular.data||[]).slice(0,4)}/></Status></section>
 <section className="home-section"><div className="section-heading"><h2>게시판 인기글</h2><a href="#/board">게시판 바로가기 →</a></div><Status state={posts}><PostList compact posts={(posts.data||[]).filter(p=>p.category!=='NOTICE').slice(0,2)}/></Status></section></>
}
export function Explore({params}){
 const keyword=params.get('keyword')||'',genre=params.get('genre')||'',platform=params.get('platform')||'',popular=params.get('popular')==='true'
 const genres=useData('/genres',[]),platforms=useData('/platforms',[])
 const list=useData('/webtoons'+(popular?'/popular':'')+query({keyword,genre,platform}),[])
 const update=(key,value)=>go('/explore'+query({...Object.fromEntries(params),[key]:value}))
 return <><h1>{popular?'인기 웹툰':'웹툰 탐색'}</h1><div className="filters">
 <select aria-label="플랫폼" value={platform} onChange={e=>update('platform',e.target.value)}><option value="">플랫폼 · 전체</option>{(platforms.data||[]).map(x=><option key={x.id} value={x.id}>{x.name}</option>)}</select>
 <select aria-label="장르" value={genre} onChange={e=>update('genre',e.target.value)}><option value="">장르 · 전체</option>{(genres.data||[]).map(x=><option key={x.id} value={x.id}>{x.name}</option>)}</select></div>
 <Status state={list}><p className="muted search-count">{keyword&&'“'+keyword+'” · '}검색 결과 {(list.data||[]).length}건</p><WebtoonGrid webtoons={list.data||[]}/></Status></>
}
export function WebtoonDetail({id}){
 const {user}=useAuth();const [version,refresh]=useState(0)
 const path='/webtoons/'+encodeURIComponent(id);const detail=useData(path,null,version),reviews=useData(path+'/reviews',[],version);const w=detail.data
 const favorite=async()=>{if(!user){go('/login');return}try{await request(path+'/favorite',w.isFavorite?'DELETE':'POST');refresh(x=>x+1)}catch(e){alert(e.message)}}
 return <Status state={detail}>{w&&<><h1>{w.title}</h1><div className="detail">
 {w.thumbnailUrl?<img src={w.thumbnailUrl} alt={w.title+' 표지'}/>:<div className="cover">표지</div>}
 <div className="detail-info"><p>작가 {[...new Set([w.storyWriter,w.pictureWriter].filter(Boolean))].join(' · ')||'정보 없음'} · 장르 {w.genre||'정보 없음'} · 플랫폼 {w.platforms?.join(', ')||'정보 없음'}</p><h3>줄거리</h3><p>{w.description||'줄거리 정보 없음'}</p><button onClick={favorite}>{w.isFavorite?'♥ 좋아하는 작품에서 해제':'♡ 좋아하는 작품에 추가'}</button></div></div>
 <section><h2>작품 댓글</h2><Comments path={path+'/comments'} webtoon/></section>
 <section><div className="section-heading"><h2>관련 리뷰</h2>{user&&<a href={'#/board/write?category=REVIEW&webtoonId='+encodeURIComponent(id)}>리뷰 작성 →</a>}</div><Status state={reviews}><PostList posts={reviews.data||[]}/></Status></section></>}</Status>
}
