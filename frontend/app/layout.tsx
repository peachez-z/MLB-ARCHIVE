import './globals.css'
import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import AppBar from "@/app/AppBar";
import {Providers} from "@/app/redux/provider";

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'MLB Archive',
  description: 'Generated by Dream the Rock',
}

declare global {
  interface Window {
    naver:any;
    Kakao:any;
    google:any;
  }
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <Providers>
          {children}
        </Providers>
      </body>
    </html>
  )
}
