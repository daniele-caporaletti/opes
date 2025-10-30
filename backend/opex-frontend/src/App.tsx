// src/App.tsx
import { Routes, Route, Navigate } from 'react-router-dom';
import Home from './pages/Home';
import OnboardingAllInOne from './pages/OnboardingAllInOne';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/onboarding" element={<OnboardingAllInOne />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
