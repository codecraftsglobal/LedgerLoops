import React, { Component } from 'react';
import PropTypes from 'prop-types';

import './style.scss';

import logo from 'assets/images/brand/ledgerloop-logo.svg';

const propTypes = {
  children: PropTypes.node,
};

const defaultProps = {};

class Footer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      language: 'en',
    };
  }
  render() {
    const { version } = this.props;

    return (
      <React.Fragment>
        <div className="d-flex align-items-center justify-content-between w-100">
          <img src={logo} className="m-2 footer-logo" alt="LedgerLoop logo" />
          <div className="pull-right" style={{ borderBlockColor: '#0f766e' }}>
            {/* <select style={{ borderBlockColor: '#0f766e' }}
						onChange={this.renderHandleLanguageChange}>
						<option value="en">En- English</option>
						<option value="it">Fr- French</option>
					</select> */}
            <i>Change Language : </i>
            <select
              defaultValue={window['localStorage'].getItem('language')}
              style={{ color: '#0f766e', width: '100px', border: 'outset' }}
              onChangeCapture={e => {
                let lang = e.target.value;
                localStorage.setItem('language', lang);
                window.location.reload(false);
              }}
            >
              <option value="en">English</option>
              <option value="it">French</option>
              <option value="ar">Arabic</option>
            </select>
          </div>
        </div>
      </React.Fragment>
    );
  }
}

Footer.propTypes = propTypes;
Footer.defaultProps = defaultProps;

export default Footer;
