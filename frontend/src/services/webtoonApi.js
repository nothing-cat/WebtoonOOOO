import {request,query} from './api.js'
export function normalizeWebtoon(w){return w?{...w,image:w.thumbnailUrl||w.image||'',pictrWritrNm:w.pictureWriter||w.pictrWritrNm||'',sntncWritrNm:w.storyWriter||w.sntncWritrNm||'',ageGrade:w.ageRating||w.ageGrade||'',platform:w.platforms?.[0]||''}:null}
export async function searchWebtoons({keyword='',genre='',platform='',popular=false}={}){return (await request('/webtoons'+(popular?'/popular':'')+query({keyword,genre,platform}))).map(normalizeWebtoon)}
export async function fetchWebtoonById(id){return normalizeWebtoon(await request('/webtoons/'+encodeURIComponent(id)))}
