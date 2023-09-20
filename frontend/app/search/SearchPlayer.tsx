import {useDispatch, useSelector} from "react-redux";
import {useState} from "react";
import Swal from "sweetalert2"
import {fetchPlayerWordData} from "@/app/redux/features/searchPlayerSlice";
import {useRouter} from "next/navigation";



const SearchPlayer = () => {
  const router = useRouter()
  const dispatch = useDispatch()
  const [nowPage, setNowPage] = useState(0)
  const [searchData, setSearchData] = useState('')
  const playerResult = useSelector((state: any) => state.searchPlayer.wordParseResult)
  // 검색 이후 전체 결과 게시물 수 전달 받아서 변수로 할당한 뒤에 최댓값 이상으로 페이지 이동하지 않게

  const searchQuery = (x:number) => {
    if (x === 0 || nowPage + x < 0) {
      setNowPage(0)
    }
    else {
      setNowPage(nowPage+x)
    }
    const action = {
      searchData: searchData, nowPage:nowPage, articlePerPage:30
    }
    dispatch(fetchPlayerWordData(action))
  }

  const searchPlayer = () => {
    console.log(searchData)
    if (searchData.length < 2) {
      Swal.fire({
        title: '경 고',
        icon: 'warning',
        iconColor: 'red',
        text: '검색어는 2글자 이상이어야 합니다.'
      })
    }
    else {
      searchQuery(0)
    }
  }
  return (
    <>
      <div>
        <h1>
          검색할 선수의 이름을 입력해 주세요.
        </h1>

        <input
          type="text"
          autoFocus
          value={searchData}
          onChange={e => setSearchData(e.target.value)}
        />

        <button onClick={searchPlayer}>
          확인
        </button>
        {
          playerResult?.length > 0 ? (
            <div>
              {
                playerResult.map((player: any) => (
                  <div key={player.id} onClick={() => router.push(`/players/${player.id}`)}>
                    {player.id}
                    {player.name}
                  </div>
                ))
              }
              <div>
                <button onClick={() => searchQuery(-1)}>이전 페이지</button>
                <button onClick={() => searchQuery(+1)}>다음 페이지</button>
              </div>
            </div>
          ) : (
            <div>검색된 선수가 없습니다</div>
          )
        }
        <div>
          <button onClick={() => searchQuery(-1)}>이전 페이지</button>
          <button onClick={() => searchQuery(+1)}>다음 페이지</button>
        </div>
      </div>
    </>
  )
}

export default SearchPlayer