import React, { useState, useEffect } from "react";
import axios from "../axios";
import { useNavigate } from "react-router-dom";
import "../assets/auth.css";
import { toast } from "react-toastify";
import { useAuth } from "../Context/AuthContext";
import { Modal, Button, Form, Spinner } from "react-bootstrap";

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [isSignUp, setIsSignUp] = useState(false);
  const [loading, setLoading] = useState(false);

  const [registerData, setRegisterData] = useState({
    username: "",
    email: "",
    password: "",
  });

  const [showVerifyModal, setShowVerifyModal] = useState(false);
  const [verifyEmail, setVerifyEmail] = useState("");
  const [verifyOtp, setVerifyOtp] = useState("");
  const [verifying, setVerifying] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(0);

  const [showForgotModal, setShowForgotModal] = useState(false);
  const [forgotStep, setForgotStep] = useState(1);
  const [forgotEmail, setForgotEmail] = useState("");
  const [resetOtp, setResetOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [forgotLoading, setForgotLoading] = useState(false);

  useEffect(() => {
    let timer;
    if (resendCooldown > 0) {
      timer = setTimeout(() => setResendCooldown(resendCooldown - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [resendCooldown]);

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!identifier.trim() || !password) {
      toast.warning("Please enter your username/email and password.");
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post("/auth/login", {
        username: identifier.trim(),
        password,
      });

      const { token, role, username: authUsername } = response.data;
      login({ token, role: role || "USER", username: authUsername || identifier });
      toast.success(`Welcome back, ${authUsername || identifier}!`);
      navigate("/");
    } catch (error) {
      if (error.response?.status === 403 && error.response?.data?.requiresVerification) {
        toast.info("Please verify your account before logging in.");
        setVerifyEmail(error.response.data.email || identifier);
        setShowVerifyModal(true);
        setResendCooldown(30);
      } else {
        const errorMsg = error.response?.data?.error || "Invalid username/email or password.";
        toast.error(errorMsg);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!registerData.username.trim() || !registerData.email.trim() || !registerData.password) {
      toast.warning("Please complete all registration fields.");
      return;
    }
    if (registerData.password.length < 6) {
      toast.warning("Password must be at least 6 characters.");
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post("/auth/register", {
        username: registerData.username.trim(),
        email: registerData.email.trim(),
        password: registerData.password,
      });

      if (response.data?.otp) {
        setVerifyOtp(response.data.otp);
        toast.info(`Verification code: ${response.data.otp}`);
      } else {
        toast.success(response.data?.message || "Verification code sent! Please check your email.");
      }
      setVerifyEmail(registerData.email.trim());
      setShowVerifyModal(true);
      setResendCooldown(60);

      setRegisterData({ username: "", email: "", password: "" });
    } catch (error) {
      const errorMsg = error.response?.data?.error || "Registration failed. Please try again.";
      toast.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    if (!verifyOtp.trim() || verifyOtp.trim().length !== 6) {
      toast.warning("Please enter a valid 6-digit verification code.");
      return;
    }

    setVerifying(true);
    try {
      const response = await axios.post("/auth/verify-account", {
        email: verifyEmail,
        otp: verifyOtp.trim(),
      });

      const { token, role, username: authUsername } = response.data;
      login({ token, role: role || "USER", username: authUsername });

      toast.success("Account successfully verified! Welcome aboard.");
      setShowVerifyModal(false);
      setVerifyOtp("");
      navigate("/");
    } catch (error) {
      const errorMsg = error.response?.data?.error || "Invalid or expired verification code.";
      toast.error(errorMsg);
    } finally {
      setVerifying(false);
    }
  };

  const handleResendVerificationOtp = async () => {
    if (resendCooldown > 0) return;
    try {
      const response = await axios.post("/auth/resend-verification-otp", {
        email: verifyEmail,
      });
      if (response.data?.otp) {
        setVerifyOtp(response.data.otp);
        toast.info(`New verification code: ${response.data.otp}`);
      } else {
        toast.success(response.data?.message || "A new verification code has been dispatched.");
      }
      setResendCooldown(60);
    } catch (error) {
      const errorMsg = error.response?.data?.error || "Failed to resend code. Please try again.";
      toast.error(errorMsg);
    }
  };

  const handleRequestPasswordReset = async (e) => {
    e.preventDefault();
    if (!forgotEmail.trim()) {
      toast.warning("Please enter your registered email address.");
      return;
    }

    setForgotLoading(true);
    try {
      const response = await axios.post("/auth/forgot-password", {
        email: forgotEmail.trim(),
      });
      if (response.data?.otp) {
        setResetOtp(response.data.otp);
        toast.info(`Password reset code: ${response.data.otp}`);
      } else {
        toast.success(response.data?.message || "Password reset code sent to your email.");
      }
      setForgotStep(2);
    } catch (error) {
      const errorMsg = error.response?.data?.error || "Unable to send reset code. Please check your email.";
      toast.error(errorMsg);
    } finally {
      setForgotLoading(false);
    }
  };

  const handleConfirmPasswordReset = async (e) => {
    e.preventDefault();
    if (!resetOtp.trim() || resetOtp.trim().length !== 6) {
      toast.warning("Please enter the 6-digit reset code.");
      return;
    }
    if (newPassword.length < 6) {
      toast.warning("Password must be at least 6 characters.");
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.warning("Passwords do not match.");
      return;
    }

    setForgotLoading(true);
    try {
      const response = await axios.post("/auth/reset-password", {
        email: forgotEmail.trim(),
        otp: resetOtp.trim(),
        newPassword,
      });

      toast.success(response.data?.message || "Password successfully reset! Please sign in.");
      setShowForgotModal(false);
      setForgotStep(1);
      setForgotEmail("");
      setResetOtp("");
      setNewPassword("");
      setConfirmPassword("");
      setIsSignUp(false);
    } catch (error) {
      const errorMsg = error.response?.data?.error || "Password reset failed. Invalid or expired code.";
      toast.error(errorMsg);
    } finally {
      setForgotLoading(false);
    }
  };

  return (
    <>
      <div className={`auth-container ${isSignUp ? "active" : ""}`}>
        <div className="auth-card">
        
        <div className="form-container sign-in-container">
            <form className="login-form signin-form" onSubmit={handleLogin}>
              <h2>Sign In</h2>
              <p className="text-muted small mb-4">Enter your credentials to access your account</p>

              <input
                type="text"
                placeholder="Username or Email"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                required
                autoComplete="username"
              />

              <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
              />

              <div
                className="d-flex align-items-center justify-content-between p-2 mt-2 rounded"
                style={{
                  background: "rgba(196, 91, 44, 0.08)",
                  border: "1px dashed #e07a3f",
                  width: "100%",
                }}
              >
                <div className="small text-start">
                  <span className="fw-bold" style={{ color: "#c45b2c" }}>Admin Demo:</span>{" "}
                  <code>admin</code> / <code>admin</code>
                </div>
                <button
                  type="button"
                  className="btn btn-sm btn-outline-primary py-0 px-2"
                  style={{ fontSize: "11px" }}
                  onClick={() => {
                    setIdentifier("admin");
                    setPassword("admin");
                  }}
                >
                  Auto-Fill
                </button>
              </div>

              <button
                type="button"
                className="forgot-password btn btn-link p-0 text-decoration-none"
                onClick={() => {
                  setForgotStep(1);
                  setForgotEmail(identifier.includes("@") ? identifier : "");
                  setShowForgotModal(true);
                }}
              >
                Forgot your password?
              </button>

              <button type="submit" className="login-btn mt-3" disabled={loading}>
                {loading ? (
                  <>
                    <Spinner animation="border" size="sm" className="me-2" />
                    Signing In...
                  </>
                ) : (
                  "SIGN IN"
                )}
              </button>

              <p className="mt-3 text-muted small text-center">
                Don't have an account?{" "}
                <button
                  type="button"
                  className="btn btn-link p-0 fw-semibold text-decoration-none"
                  style={{ color: "#6C63FF" }}
                  onClick={() => setIsSignUp(true)}
                >
                  Sign Up
                </button>
              </p>
            </form>
          </div>

          <div className="form-container sign-up-container">
            <form className="login-form signup-form" onSubmit={handleRegister}>
              <h2>Create Account</h2>
              <p className="text-muted small mb-3">Sign up with your verified email</p>

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
                autoComplete="username"
              />

              <input
                type="email"
                placeholder="Email Address"
                value={registerData.email}
                onChange={(e) =>
                  setRegisterData({
                    ...registerData,
                    email: e.target.value,
                  })
                }
                required
                autoComplete="email"
              />

              <input
                type="password"
                placeholder="Password (min 6 characters)"
                value={registerData.password}
                onChange={(e) =>
                  setRegisterData({
                    ...registerData,
                    password: e.target.value,
                  })
                }
                required
                minLength={6}
                autoComplete="new-password"
              />

              <button type="submit" className="login-btn mt-3" disabled={loading}>
                {loading ? (
                  <>
                    <Spinner animation="border" size="sm" className="me-2" />
                    Creating Account...
                  </>
                ) : (
                  "SIGN UP"
                )}
              </button>

              <p className="mt-3 text-muted small text-center">
                Already have an account?{" "}
                <button
                  type="button"
                  className="btn btn-link p-0 fw-semibold text-decoration-none"
                  style={{ color: "#6C63FF" }}
                  onClick={() => setIsSignUp(false)}
                >
                  Sign In
                </button>
              </p>
            </form>
          </div>

          <div className="overlay-container">
            <div className="overlay">
              <div className="overlay-panel overlay-left">
                <h1>Welcome Back!</h1>
                <p>Already have an account? Sign in with your registered credentials.</p>
                <button
                  className="ghost"
                  type="button"
                  onClick={() => setIsSignUp(false)}
                >
                  SIGN IN
                </button>
              </div>

              <div className="overlay-panel overlay-right">
                <h1>Hello, Friend!</h1>
                <p>New to our store? Create an account and verify your email to get started.</p>
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

      <Modal
        show={showVerifyModal}
        onHide={() => !verifying && setShowVerifyModal(false)}
        centered
        backdrop="static"
        dialogClassName="auth-modal-dialog"
      >
        <div className="auth-modal-card card shadow-lg">
          <Modal.Header closeButton={!verifying} className="border-0 pb-0">
            <Modal.Title className="fw-bold fs-5 text-primary">
              <i className="bi bi-shield-check me-2"></i>Verify Your Account
            </Modal.Title>
          </Modal.Header>
          <Modal.Body className="px-4 py-3">
            <p className="text-muted small">
              We dispatched a 6-digit verification code to:
              <br />
              <strong className="text-dark fs-6">{verifyEmail}</strong>
            </p>
            <Form onSubmit={handleVerifyOtp}>
              <Form.Group className="mb-3">
                <Form.Label className="small fw-semibold text-secondary">
                  6-Digit Verification Code
                </Form.Label>
                <Form.Control
                  type="text"
                  maxLength={6}
                  placeholder="• • • • • •"
                  className="otp-digit-input"
                  value={verifyOtp}
                  onChange={(e) => setVerifyOtp(e.target.value.replace(/\D/g, ""))}
                  autoFocus
                  required
                />
              </Form.Group>
              <div className="d-grid mb-3">
                <Button variant="primary" type="submit" disabled={verifying || verifyOtp.length !== 6}>
                  {verifying ? (
                    <>
                      <Spinner animation="border" size="sm" className="me-2" />
                      Verifying Code...
                    </>
                  ) : (
                    "Verify & Activate Account"
                  )}
                </Button>
              </div>
            </Form>
            <div className="text-center">
              <button
                type="button"
                className="btn btn-sm btn-link text-decoration-none"
                onClick={handleResendVerificationOtp}
                disabled={resendCooldown > 0}
              >
                {resendCooldown > 0
                  ? `Resend Code in ${resendCooldown}s`
                  : "Didn't receive the code? Resend Code"}
              </button>
            </div>
          </Modal.Body>
        </div>
      </Modal>

      <Modal
        show={showForgotModal}
        onHide={() => !forgotLoading && setShowForgotModal(false)}
        centered
        dialogClassName="auth-modal-dialog"
      >
        <div className="auth-modal-card card shadow-lg">
          <Modal.Header closeButton={!forgotLoading} className="border-0 pb-0">
            <Modal.Title className="fw-bold fs-5 text-primary">
              <i className="bi bi-key me-2"></i>
              {forgotStep === 1 ? "Forgot Password" : "Reset Password"}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body className="px-4 py-3">
            {forgotStep === 1 ? (
              <Form onSubmit={handleRequestPasswordReset}>
                <p className="text-muted small mb-3">
                  Enter your registered email address and we'll send you a 6-digit recovery code.
                </p>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Email Address</Form.Label>
                  <Form.Control
                    type="email"
                    placeholder="name@example.com"
                    value={forgotEmail}
                    onChange={(e) => setForgotEmail(e.target.value)}
                    required
                    autoFocus
                  />
                </Form.Group>
                <div className="d-grid">
                  <Button variant="primary" type="submit" disabled={forgotLoading}>
                    {forgotLoading ? (
                      <>
                        <Spinner animation="border" size="sm" className="me-2" />
                        Sending Code...
                      </>
                    ) : (
                      "Send Recovery Code"
                    )}
                  </Button>
                </div>
              </Form>
            ) : (
              <Form onSubmit={handleConfirmPasswordReset}>
                <p className="text-muted small mb-3">
                  Enter the 6-digit code sent to <strong>{forgotEmail}</strong> and your new password.
                </p>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">6-Digit Reset Code</Form.Label>
                  <Form.Control
                    type="text"
                    maxLength={6}
                    placeholder="• • • • • •"
                    className="otp-digit-input"
                    value={resetOtp}
                    onChange={(e) => setResetOtp(e.target.value.replace(/\D/g, ""))}
                    required
                    autoFocus
                  />
                </Form.Group>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">New Password</Form.Label>
                  <Form.Control
                    type="password"
                    placeholder="Minimum 6 characters"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                    minLength={6}
                  />
                </Form.Group>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-semibold">Confirm New Password</Form.Label>
                  <Form.Control
                    type="password"
                    placeholder="Re-enter password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    minLength={6}
                  />
                </Form.Group>
                <div className="d-grid gap-2">
                  <Button variant="primary" type="submit" disabled={forgotLoading}>
                    {forgotLoading ? (
                      <>
                        <Spinner animation="border" size="sm" className="me-2" />
                        Updating Password...
                      </>
                    ) : (
                      "Reset Password & Sign In"
                    )}
                  </Button>
                  <Button
                    variant="outline-secondary"
                    size="sm"
                    onClick={() => setForgotStep(1)}
                    disabled={forgotLoading}
                  >
                    Back
                  </Button>
                </div>
              </Form>
            )}
          </Modal.Body>
        </div>
      </Modal>
    </>
  );
};

export default Login;
