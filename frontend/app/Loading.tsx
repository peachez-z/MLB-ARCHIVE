import React, { useState, useEffect } from "react";
import "../styles/Loading.css";
import logo from "../assets/intro/intro_pull.png";
import ball from "../assets/ball.png";
import Image from "next/image";

function Loading() {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
      }}
    >
      <Image className="loading_logo" src={logo} alt="로고" />
      <Image className="loading_ball" src={ball} alt="공" />
    </div>
  );
}

export default Loading;
