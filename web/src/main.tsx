import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import './index.css'
import Home from './pages/Home'
import Question from './pages/Question'
import Leaderboard from './pages/Leaderboard'
import CustomerHome from './pages/CustomerHome'
import CustomerQuestion from './pages/CustomerQuestion'
import CustomerLeaderboard from './pages/CustomerLeaderboard'

function App() {
  return (
    <BrowserRouter>
      <nav className="p-4 flex gap-4 bg-neutral-800">
        <Link to="/">Admin Home</Link>
        <Link to="/leaderboard">Admin Leaderboard</Link>
        <Link to="/customer">Customer Home</Link>
        <Link to="/customer/leaderboard">Customer Leaderboard</Link>
      </nav>
      <div className="p-6 max-w-4xl mx-auto">
        <Routes>
          <Route path="/" element={<Home/>} />
          <Route path="/q/:id" element={<Question/>} />
          <Route path="/leaderboard" element={<Leaderboard/>} />
          <Route path="/customer" element={<CustomerHome/>} />
          <Route path="/customer/q/:id" element={<CustomerQuestion/>} />
          <Route path="/customer/leaderboard" element={<CustomerLeaderboard/>} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}

const root = createRoot(document.getElementById('root')!)
root.render(<App/>)