export const genres=[{id:'fantasy',name:'판타지'},{id:'romance',name:'로맨스'},{id:'action',name:'액션'},{id:'drama',name:'드라마'},{id:'daily',name:'일상'}]
export const platforms=[{id:'naver',name:'네이버웹툰'},{id:'kakao',name:'카카오웹툰'},{id:'ridi',name:'리디'}]
export const webtoons=[
['W001','달빛 우체국','서하','판타지','fantasy',['naver','ridi'],'#7772ac',18],
['W002','여름의 끝','이안','로맨스','romance',['kakao'],'#cc8497',12],
['W003','검의 계절','하루','액션','action',['naver'],'#597d90',25],
['W004','고양이와 오후','나무','일상','daily',['ridi'],'#b7a36c',8],
['W005','별의 도서관','윤슬','판타지','fantasy',['kakao','ridi'],'#6d91a7',11],
['W006','우리의 거리','소담','로맨스','romance',['naver'],'#a78a9e',9]
].map(([id,title,pictureWriter,genre,genreId,platformIds,color,likeCount])=>({id,title,pictureWriter,storyWriter:pictureWriter,genre,genreId,platformIds,platforms:platformIds.map(id=>platforms.find(p=>p.id===id).name),description:title+' — 가상의 웹툰 소개입니다.',thumbnailUrl:null,ageRating:'전체 이용가',color,likeCount}))
export const categories={NOTICE:'공지사항',GENERAL:'일반',REVIEW:'리뷰',QUESTION:'질문'}
export const initialPosts=[
{id:1,userId:'admin',username:'admin',category:'NOTICE',title:'웹툰 커뮤니티 이용 안내',content:'서로 존중하며 이야기를 나눠 주세요.',viewCount:32,likeCount:5},
{id:2,userId:'demo',username:'demo',category:'REVIEW',webtoonId:'W001',title:'달빛 우체국 감상 후기',content:'잔잔한 분위기가 좋았어요.',viewCount:21,likeCount:9},
{id:3,userId:'demo',username:'demo',category:'QUESTION',title:'판타지 웹툰 추천 부탁드려요',content:'재미있는 작품을 찾고 있습니다.',viewCount:12,likeCount:3}
].map(p=>({...p,createdAt:'2026-11-24T09:00:00Z',commentCount:0}))
