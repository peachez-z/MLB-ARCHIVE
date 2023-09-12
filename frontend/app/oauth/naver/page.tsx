'use client'
import {useEffect, useState} from "react";
import {useDispatch} from "react-redux";
import axios from "axios";
import {fetchUserData} from "@/app/redux/features/userSlice";

const NaverLoginPage = () => {
  let code: string | null = null;
  let state: string | null = null;
  if (typeof window !== 'undefined') {
    code = window.location.href.split("code=")[1]?.split("&")[0];
    state = window.location.href.split("state=")[1]?.split("&")[0];
  }
  const dispatch = useDispatch()
  useEffect(() => {
    const accessKey = {
      code: code ?? "",
      state: state ?? "",
      kind: 'naver',
    }
    dispatch(fetchUserData(accessKey))
  }, [code])
  return (
    <>
      <div>
        네이버 로그인 로직 진행 중
      </div>
    </>
  )
}

export default NaverLoginPage