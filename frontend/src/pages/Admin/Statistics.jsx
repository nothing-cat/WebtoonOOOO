import {useState} from 'react'
import {useData} from '../../hooks/useData.js'
import {request} from '../../services/apiClient.js'
import {Status} from '../shared.jsx'

const number = new Intl.NumberFormat('ko-KR')

export default function Statistics() {
  const [version, setVersion] = useState(0)
  const statistics = useData(() => request('/admin/statistics'), 'admin-statistics', null, version)

  return <section>
    <h2>이용자 통계</h2>
    <button type="button" disabled={statistics.loading} onClick={() => setVersion(value => value + 1)}>
      새로고침
    </button>
    <Status state={statistics}>
      {statistics.data && <>
        <p>{statistics.data.date} · 한국 시간 기준</p>
        <dl className="admin-statistics">
          <div>
            <dt>오늘 접속자</dt>
            <dd>{number.format(statistics.data.dailyVisitors)}명</dd>
          </div>
          <div>
            <dt>전체 가입 회원</dt>
            <dd>{number.format(statistics.data.totalMembers)}명</dd>
          </div>
        </dl>
        <p className="muted">오늘 접속자는 로그인 여부와 관계없이 접속한 브라우저를 하루에 한 번 집계해요.</p>
      </>}
    </Status>
  </section>
}
