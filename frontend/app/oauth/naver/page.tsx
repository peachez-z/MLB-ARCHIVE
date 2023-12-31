'use client'
import {useEffect, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import axios from "axios";
import {fetchUserData} from "@/app/redux/features/userSlice";
import {useRouter} from "next/navigation";
import Loading from "@/app/Loading";

const NaverLoginPage = () => {
  const router = useRouter()
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
  const isLoggedIn = useSelector((state:any) => !!state.user?.isLoggedIn)
  const userId = useSelector((state:any) => state.user.userData?.userId)
  useEffect(() => {
    if (isLoggedIn) {
      router.push("/main");
    }
  }, [isLoggedIn])
  return (
    <>
      <Loading />
    </>
  )
}

export default NaverLoginPage