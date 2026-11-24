import {useState} from 'react'
import {useSearchParams} from 'react-router-dom'
import {useData} from '../../hooks/useData.js'
import {query} from '../../services/api.js'
import {normalizeWebtoon} from '../../services/webtoonApi.js'
import FilterPanel from '../../components/FilterPanel/FilterPanel.jsx'
import SearchBar from '../../components/SearchBar/SearchBar.jsx'
import WebtoonGrid from '../../components/Webtoon/WebtoonGrid.jsx'
import Button from '../../components/ui/Button.jsx'
import SectionTitle from '../../components/ui/SectionTitle.jsx'
import {Status} from '../shared.jsx'
import './Explore.css'
export default function Explore(){const [params,setParams]=useSearchParams();const keyword=params.get('keyword')||'',genre=params.get('genre')||'',platform=params.get('platform')||'',popular=params.get('popular')==='true';const [input,setInput]=useState(null),[show,setShow]=useState(true);const genres=useData('/genres',[]),platforms=useData('/platforms',[]),list=useData('/webtoons'+(popular?'/popular':'')+query({keyword,genre,platform}),[]);function change(values){setParams(Object.fromEntries(Object.entries({...Object.fromEntries(params),...values}).filter(([,v])=>v)))}
return <div className="page"><div className="card page-heading"><p className="eyebrow">Explore</p><h1>{popular?'인기 웹툰':'웹툰 탐색'}</h1><p>작품명, 작가를 검색하고 장르와 플랫폼으로 원하는 웹툰을 찾아보세요.</p></div><SectionTitle title="통합 검색"/><section className="explore-toolbar"><SearchBar keyword={input??keyword} onKeywordChange={setInput} onEnter={()=>{change({keyword:input??keyword});setInput(null)}}/><Button variant="outline" size="medium" onClick={()=>setShow(v=>!v)}>필터</Button><Button onClick={()=>{change({keyword:input??keyword});setInput(null)}}>검색</Button></section>{show&&<FilterPanel filters={{genre,platform}} genres={genres.data||[]} platforms={platforms.data||[]} onChange={change}/>}<section><SectionTitle eyebrow="Search Result" title={list.loading?'검색 중...':(list.data||[]).length+'개의 웹툰'} compact/><Status state={list}><WebtoonGrid webtoons={(list.data||[]).map(normalizeWebtoon)}/></Status></section></div>}
