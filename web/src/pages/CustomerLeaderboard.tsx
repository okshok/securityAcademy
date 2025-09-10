import React, { useEffect, useState } from 'react'
import { api } from '../lib/api'

export default function CustomerLeaderboard() {
  const [rows, setRows] = useState<any[]>([])
  useEffect(()=>{ api.get('/customer/leaderboard').then(r=> setRows(r.data)) },[])
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">시즌 리더보드</h1>
      <table className="w-full text-left">
        <thead><tr><th>랭크</th><th>유저</th><th>점수</th></tr></thead>
        <tbody>
          {rows.map((r,i)=> (
            <tr key={i} className="border-t border-neutral-800">
              <td>{r.rank}</td><td>{r.userId}</td><td>{r.totalPoints}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
