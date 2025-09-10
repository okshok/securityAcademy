import React, { useEffect, useState } from 'react'
import { api } from '../lib/api'

export default function Home(){
  const [drafts,setDrafts]=useState<any[]>([])
  useEffect(()=>{ api.get('/questions/drafts').then(r=> setDrafts(r.data)) },[])
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">후보 문제(오늘)</h1>
      <div className="space-y-3">
        {drafts.map(d=>(
          <div key={d.id} className="p-3 rounded-lg bg-neutral-800">
            <div className="opacity-80 text-sm">{d.type} {d.ticker||''}</div>
            <div className="font-semibold">{d.prompt}</div>
          </div>
        ))}
      </div>
    </div>
  )
}