import React, { useState } from "react";
import axios from "../axios";
import { useNavigate } from "react-router-dom";
import "../assets/auth.css";
import { toast } from "react-toastify";


import {
  FaGoogle,
  FaFacebookF,
  FaGithub,
  FaLinkedinIn,
} from "react-icons/fa";

const Login = () => {
  const navigate = useNavigate();

  // Login States
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  

  // Toggle Login/Register
  const [isSignUp, setIsSignUp] = useState(false);

  // Register States
  const [registerData, setRegisterData] = useState({
    username: "",
    
    password: "",
  });

  // Login API
  const handleLogin = async (e) => {
  e.preventDefault();

  try {
    const response = await axios.post(
      "http://localhost:8080/auth/login",
      {
        username,
        password,
      }
    );

    localStorage.setItem("token", response.data.token);

    toast.success("Login Successful");

    navigate("/");
  } catch (error) {
    toast.error("Invalid Username or Password");
    console.error(error);
  }
};

// 👇 ADD THIS HERE
const handleRegister = async (e) => {
  e.preventDefault();
  console.log("Register button clicked");

  try {
    await axios.post(
      "http://localhost:8080/auth/register",
      {
        username: registerData.username,
        password: registerData.password,
        role: "USER",
      }
    );

    toast.success("Registration Successful");

    setRegisterData({
      username: "",
      password: "",
    });

    setIsSignUp(false);

  } catch (error) {
    toast.error("Registration Failed");
    console.error(error);
  }
};

  return (
    <div className={`auth-container ${isSignUp ? "active" : ""}`}>
      <div className="auth-card">

        {/* ==================== SIGN IN ==================== */}

        <div className="form-container sign-in-container">
          <form className="login-form" onSubmit={handleLogin}>

            <h2>Sign In</h2>

            <div className="social-container">

              <button type="button" className="social">
                <FaGoogle />
              </button>

              <button type="button" className="social">
                <FaFacebookF />
              </button>

              <button type="button" className="social">
                <FaGithub />
              </button>

              <button type="button" className="social">
                <FaLinkedinIn />
              </button>

            </div>

            <span>or use your account</span>

            <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />

            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />

            <a
              href="/"
              className="forgot-password"
              onClick={(e) => e.preventDefault()}
            >
              Forgot your password?
            </a>

            <button type="submit" className="login-btn">
              SIGN IN
            </button>

          </form>
        </div>

        {/* ==================== SIGN UP ==================== */}

        <div className="form-container sign-up-container">

          <form className="login-form signup-form" onSubmit={handleRegister}>

            <h2>Create Account</h2>

            <div className="social-container">

              <button type="button" className="social">
                <FaGoogle />
              </button>

              <button type="button" className="social">
                <FaFacebookF />
              </button>

              <button type="button" className="social">
                <FaGithub />
              </button>

              <button type="button" className="social">
                <FaLinkedinIn />
              </button>

            </div>

            <span>or register using your email</span>

            <input
              type="text"
              placeholder="Username"
              value={registerData.username}
              onChange={(e) =>
                setRegisterData({
                  ...registerData,
                  username: e.target.value,
                })
              }
              required
            />

            

            <input
              type="password"
              placeholder="Password"
              value={registerData.password}
              onChange={(e) =>
                setRegisterData({
                  ...registerData,
                  password: e.target.value,
                })
              }
              required
            />

            <button
              type="submit"
              className="login-btn"
            >
              SIGN UP
            </button>

          </form>

        </div>

        {/* ==================== OVERLAY ==================== */}

        <div className="overlay-container">

          <div className="overlay">

            {/* LEFT */}

            <div className="overlay-panel overlay-left">

              <h1>Welcome Back!</h1>

              <p>
                To keep connected with us please login
                with your personal information.
              </p>

              <button
                className="ghost"
                type="button"
                onClick={() => setIsSignUp(false)}
              >
                SIGN IN
              </button>

            </div>

            {/* RIGHT */}

            <div className="overlay-panel overlay-right">

              <h1>Hello, Friend!</h1>

              <p>
                Enter your personal details and start your
                shopping journey with us.
              </p>

              <button
                className="ghost"
                type="button"
                onClick={() => setIsSignUp(true)}
              >
                SIGN UP
              </button>

            </div>

          </div>

        </div>

      </div>
    </div>
  );
};

export default Login;