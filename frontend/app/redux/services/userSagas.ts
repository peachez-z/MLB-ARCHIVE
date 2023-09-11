import { call, put, takeLatest, CallEffect, PutEffect } from "@redux-saga/core/effects";
import axios, { AxiosResponse } from "axios";
import {
  fetchUserData, fetchUserDataSuccess, fetchUserDataError, fetchUserLogout,
  addFollowTeam, removeFollowTeam, addFollowPlayer, removeFollowPlayer,
} from "@/app/redux/features/userSlice";
import {PayloadAction} from "@reduxjs/toolkit";

const baseURL = process.env.NEXT_PUBLIC_SERVER_BASE_URL
const oauthURL = process.env.NEXT_PUBLIC_OAUTH_SERVER_URL
const loginURL = process.env.NEXT_PUBLIC_SERVER_LOGIN_URL
const logoutURL = process.env.NEXT_PUBLIC_SERVER_LOGOUT_URL
const followTeamURL = ''
const followPlayerURL = ''

// ./features/userSlice.ts
interface UserData {
  id: number;
  nickname: string;
  email: string;
  profileImage: string;
  followList: any;
}

interface FetchUserDataResponse {
  userData: UserData
  accessToken: string
  refreshToken: string
}
interface FetchUserDataPayload {
  code: string;
  state: string;
  kind: string
}
function* fetchUserDataSaga(action: PayloadAction<FetchUserDataPayload>): Generator<CallEffect | PutEffect, void, AxiosResponse<FetchUserDataResponse>> {
  try {
    const code = action.payload.code
    const state = action.payload.state
    const kind = action.payload.kind

    const response: AxiosResponse<FetchUserDataResponse> = yield call(axios.get,`${baseURL}${oauthURL}${kind}?code=${code}?state=${state}`)
    if (response.data) {
      yield put(fetchUserDataSuccess(response.data))
    }
  }
  catch (error) {
    yield put(fetchUserDataError(error as Error))
  }
}

function* addFollowPlayerSaga(action: PayloadAction<any>): Generator<CallEffect | PutEffect, void, AxiosResponse<FetchUserDataResponse>> {
  try {
    const userId = action.payload.userId
    const playerId = action.payload.playerId
    const accessToken = action.payload.accessToken
    const response: AxiosResponse<any> = yield call(
      axios.put,
      `${baseURL}${followPlayerURL}?userId=${userId}?playerId=${playerId}`,
      true,
      {
        headers: {
          Authorization: accessToken
        }
      })
    if (response.data) {
      yield put(fetchUserDataSuccess(response.data))
    }
  }
  catch (error) {
    yield put(fetchUserDataError(error as Error))
  }
}
function* removeFollowPlayerSaga(action: PayloadAction<any>): Generator<CallEffect | PutEffect, void, AxiosResponse<FetchUserDataResponse>> {
  try {
    const userId = action.payload.userId
    const playerId = action.payload.playerId
    const accessToken = action.payload.accessToken
    const response: AxiosResponse<any> = yield call(
      axios.put,
      `${baseURL}${followPlayerURL}?userId=${userId}?playerId=${playerId}`,
      false,
      {
        headers: {
          Authorization: accessToken
        }
      })
    if (response.data) {
      yield put(fetchUserDataSuccess(response.data))
    }
  }
  catch (error) {
    yield put(fetchUserDataError(error as Error))
  }
}

function* addFollowTeamSaga(action: PayloadAction<any>): Generator<CallEffect | PutEffect, void, AxiosResponse<FetchUserDataResponse>> {
  try {
    const userId = action.payload.userId
    const TeamId = action.payload.TeamId
    const accessToken = action.payload.accessToken
    const response: AxiosResponse<any> = yield call(
      axios.put,
      `${baseURL}${followTeamURL}?userId=${userId}?TeamId=${TeamId}`,
      true,
      {
        headers: {
          Authorization: accessToken
        }
      })
    if (response.data) {
      yield put(fetchUserDataSuccess(response.data))
    }
  }
  catch (error) {
    yield put(fetchUserDataError(error as Error))
  }
}
function* removeFollowTeamSaga(action: PayloadAction<any>): Generator<CallEffect | PutEffect, void, AxiosResponse<FetchUserDataResponse>> {
  try {
    const userId = action.payload.userId
    const TeamId = action.payload.TeamId
    const accessToken = action.payload.accessToken
    const response: AxiosResponse<any> = yield call(
      axios.put,
      `${baseURL}${followTeamURL}?userId=${userId}?TeamId=${TeamId}`,
      false,
      {
        headers: {
          Authorization: accessToken
        }
      })
    if (response.data) {
      yield put(fetchUserDataSuccess(response.data))
    }
  }
  catch (error) {
    yield put(fetchUserDataError(error as Error))
  }
}

function* fetchUserLogoutSaga(): Generator<CallEffect | PutEffect, void, AxiosResponse<FetchUserDataResponse>> {
  const response: AxiosResponse<any> = yield call(axios.get,`${baseURL}${logoutURL}`)
}

export function* watchFetchUserData() {
  yield takeLatest(fetchUserData.type, fetchUserDataSaga)
  yield takeLatest(fetchUserLogout.type, fetchUserLogoutSaga)
  yield takeLatest(addFollowTeam.type, addFollowTeamSaga)
  yield takeLatest(removeFollowTeam.type, removeFollowTeamSaga)
  yield takeLatest(addFollowPlayer.type, addFollowPlayerSaga)
  yield takeLatest(removeFollowPlayer.type, removeFollowPlayerSaga)

}