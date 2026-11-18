// Loader component with CSS animations (replaced framer-motion to save 378KB)
import logo from 'assets/images/brand/ledgerloop-mark.svg';
import './loader.css';

export default function Loader({ loadingMsg, NextloadingMsg }) {
  return (
    <div style={{ marginTop: '18%' }}>
      <div
        className="mt-5"
        style={{
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <div className="loader-logo-container">
          <img style={{ width: 48, height: 48 }} src={logo} alt="LedgerLoop" />
        </div>

        <div className="loader-ring loader-ring-inner" />
        <div className="loader-ring loader-ring-outer" />
      </div>
      <div className="text-center mt-5" style={{ color: '#0f766e' }}>
        <b>
          {loadingMsg ? loadingMsg : 'Loading...'}
          <br />
          {NextloadingMsg ? NextloadingMsg : ''}
        </b>
      </div>
    </div>
  );
}
