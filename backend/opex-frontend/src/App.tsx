import { Routes, Route, Navigate } from 'react-router-dom';
import StepOne from './pages/StepOne';
import StepTwo from './pages/StepTwo';
import OnboardingSimple from './pages/OnboardingSimple';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<StepOne />} />
      <Route path="/profile" element={<StepTwo />} />
      <Route path="/onboarding-simple" element={<OnboardingSimple />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
