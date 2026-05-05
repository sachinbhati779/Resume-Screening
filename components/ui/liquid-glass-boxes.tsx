"use client";

import React, { useEffect, useRef } from "react";

import { cn } from "@/lib/utils";

type GlassTunnel3DProps = {
  boxCount?: number;
  circleCount?: number;
  animationDuration?: number;
  boxWidth?: number;
  boxHeight?: number;
  boxDepth?: number;
  holeSize?: number;
  circleSize?: number;
  className?: string;
};

type TunnelStyle = React.CSSProperties & {
  "--tc": number;
  "--count": number;
  "--duration": string;
  "--step": string;
  "--w": string;
  "--h": string;
  "--d": string;
  "--hd": string;
  "--hole": string;
  "--circle-size": string;
};

type IndexedStyle = React.CSSProperties & {
  "--i"?: number;
  "--j"?: number;
};

function clampNumber(
  value: number,
  fallback: number,
  min: number,
  max: number,
) {
  if (!Number.isFinite(value)) {
    return fallback;
  }

  return Math.min(Math.max(value, min), max);
}

function clampInteger(
  value: number,
  fallback: number,
  min: number,
  max: number,
) {
  return Math.round(clampNumber(value, fallback, min, max));
}

const GlassTunnel3D = ({
  boxCount = 3,
  circleCount = 4,
  animationDuration = 5,
  boxWidth = 450,
  boxHeight = 350,
  boxDepth = 10,
  holeSize = 40,
  circleSize = 150,
  className,
}: GlassTunnel3DProps) => {
  const sceneRef = useRef<HTMLDivElement | null>(null);
  const safeBoxCount = clampInteger(boxCount, 3, 1, 8);
  const safeCircleCount = clampInteger(circleCount, 4, 1, 12);
  const safeAnimationDuration = clampNumber(animationDuration, 5, 1, 30);
  const safeBoxWidth = clampNumber(boxWidth, 450, 120, 900);
  const safeBoxHeight = clampNumber(boxHeight, 350, 120, 720);
  const safeBoxDepth = clampNumber(boxDepth, 10, 1, 80);
  const safeHoleSize = clampNumber(holeSize, 40, 5, 80);
  const safeCircleSize = clampNumber(circleSize, 150, 24, 420);

  useEffect(() => {
    const scene = sceneRef.current;
    if (!scene) {
      return undefined;
    }

    const circles = scene.querySelectorAll(".tunnel-circle");
    const prefersReducedMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)",
    );

    if (prefersReducedMotion.matches) {
      circles.forEach((circle) => {
        circle.classList.add("circle-active");
      });
      return undefined;
    }

    const timer = window.setTimeout(() => {
      circles.forEach((circle) => {
        circle.classList.add("circle-active");
      });
    }, 1800);

    return () => window.clearTimeout(timer);
  }, []);

  const boxStyle: TunnelStyle = {
    "--tc": safeBoxCount,
    "--count": safeCircleCount,
    "--duration": `${safeAnimationDuration}s`,
    "--step": `calc(${safeAnimationDuration}s / ${safeCircleCount})`,
    "--w": `${safeBoxWidth}px`,
    "--h": `${safeBoxHeight}px`,
    "--d": `${safeBoxDepth}px`,
    "--hd": `${safeBoxDepth / 2}px`,
    "--hole": `${safeHoleSize}%`,
    "--circle-size": `${safeCircleSize}px`,
  };

  return (
    <div
      aria-hidden="true"
      className={cn(
        "relative flex h-screen w-full items-center justify-center overflow-hidden bg-[#0A0A0A]",
        className,
      )}
      style={{
        transformStyle: "preserve-3d",
        perspective: "10000px",
        perspectiveOrigin: "15% 51%",
      }}
    >
      <div
        className="absolute inset-0 -z-10"
        style={{
          background: `
            radial-gradient(120% 120% at 25% 20%, rgba(255,255,255,0.18), rgba(255,255,255,0.02) 35%, rgba(0,0,0,0.0) 60%),
            radial-gradient(120% 120% at 75% 80%, rgba(0,0,0,0.2), rgba(0,0,0,0.3) 60%)
          `,
        }}
      />

      <svg className="hidden" aria-hidden="true">
        <defs>
          <filter id="wave-distort" x="0%" y="0%" width="100%" height="100%">
            <feTurbulence
              type="fractalNoise"
              baseFrequency="0.0038 0.0038"
              numOctaves="1"
              seed="2"
              result="roughNoise"
            />
            <feGaussianBlur
              in="roughNoise"
              stdDeviation="8.5"
              result="softNoise"
            />
            <feComposite
              operator="arithmetic"
              k1="0"
              k2="1"
              k3="2"
              k4="0"
              in="softNoise"
              result="mergedMap"
            />
            <feDisplacementMap
              in="SourceGraphic"
              in2="mergedMap"
              scale="-42"
              xChannelSelector="G"
              yChannelSelector="G"
            />
          </filter>
        </defs>
      </svg>

      <div
        ref={sceneRef}
        className="tunnel-scene flex h-full w-full items-center justify-center"
        style={{
          ...boxStyle,
          transformStyle: "preserve-3d",
        }}
      >
        {Array.from({ length: safeBoxCount }, (_, i) => (
          <div
            key={`box-${i}`}
            className="tunnel-box absolute"
            style={
              {
                "--i": i + 1,
                width: "var(--w)",
                height: "var(--h)",
                transformStyle: "preserve-3d",
              } as IndexedStyle
            }
          >
            {["front", "back", "left", "right", "top", "bottom"].map(
              (face) => (
                <div
                  key={face}
                  className={`glass-${face} absolute bg-black/60`}
                  style={{
                    backdropFilter: "url(#wave-distort)",
                    ...(face === "front" || face === "back"
                      ? {
                          width: "var(--w)",
                          height: "var(--h)",
                          mask: "radial-gradient(circle at 50% 50%, transparent var(--hole), white var(--hole))",
                          WebkitMask:
                            "radial-gradient(circle at 50% 50%, transparent var(--hole), white var(--hole))",
                        }
                      : {}),
                    ...(face === "left" || face === "right"
                      ? {
                          width: "var(--d)",
                          height: "var(--h)",
                        }
                      : {}),
                    ...(face === "top" || face === "bottom"
                      ? {
                          width: "var(--w)",
                          height: "var(--d)",
                        }
                      : {}),
                  }}
                />
              ),
            )}
          </div>
        ))}

        {Array.from({ length: safeCircleCount }, (_, i) => (
          <div
            key={`circle-${i}`}
            className="tunnel-circle absolute rounded-full bg-white opacity-0"
            style={
              {
                "--j": i + 1,
                width: "var(--circle-size)",
                height: "var(--circle-size)",
                boxShadow: `
                  inset 28px 28px 58px rgba(0,0,0,.38),
                  inset -28px -28px 54px rgba(255,255,255,.90),
                  inset 0 0 22px rgba(0,0,0,.20),
                  inset 0 1px 2px rgba(255,255,255,.55),
                  18px 26px 36px rgba(0,0,0,.55)
                `,
              } as IndexedStyle
            }
          >
            <div
              className="absolute rounded-full"
              style={{
                inset: "18% 46% 56% 12%",
                background:
                  "radial-gradient(80% 70% at 30% 30%, rgba(255,255,255,0.95), rgba(255,255,255,0.35) 40%, rgba(255,255,255,0) 75%)",
                filter: "blur(1.5px)",
                mixBlendMode: "screen",
              }}
            />
          </div>
        ))}
      </div>

      <style jsx>{`
        @property --Ctz {
          syntax: "<length>";
          inherits: false;
          initial-value: -415px;
        }
        @property --scale {
          syntax: "<number>";
          inherits: false;
          initial-value: 1;
        }
        @property --space {
          syntax: "<length>";
          inherits: false;
          initial-value: 0px;
        }
        @property --rtY {
          syntax: "<angle>";
          inherits: false;
          initial-value: 0deg;
        }
        @property --rtX {
          syntax: "<angle>";
          inherits: false;
          initial-value: 0deg;
        }

        .tunnel-scene {
          transform: rotateY(var(--rtY)) rotateX(var(--rtX));
          animation: angle 1s ease-in-out forwards;
        }

        .tunnel-box {
          transform: translateZ(
            calc((var(--i) - (var(--tc) + 1) / 2) * var(--space))
          );
          animation: space 1s 0.8s ease-in-out forwards;
          will-change: transform;
        }

        .glass-back {
          transform: translateZ(calc(-1 * var(--d))) rotateY(180deg);
        }

        .glass-left {
          transform: translateZ(calc(-1 * var(--hd)))
            translateX(calc(-1 * var(--hd))) rotateY(90deg);
        }

        .glass-right {
          right: 0;
          transform: translateZ(calc(-1 * var(--hd))) translateX(var(--hd))
            rotateY(90deg);
        }

        .glass-top {
          transform: translateZ(calc(-1 * var(--hd)))
            translateY(calc(-1 * var(--hd))) rotateX(90deg);
        }

        .glass-bottom {
          bottom: 0;
          transform: translateZ(calc(-1 * var(--hd))) translateY(var(--hd))
            rotateX(90deg);
        }

        .circle-active {
          transform: translateZ(var(--Ctz)) rotate(45deg) rotateY(1deg)
            rotateX(88deg) scale(var(--scale));
          animation:
            move var(--duration) linear infinite,
            fade var(--duration) linear infinite;
          animation-delay: calc((var(--j) - 1) * var(--step));
          will-change: transform, opacity;
        }

        @keyframes angle {
          0% {
            --rtY: 0deg;
            --rtX: 0deg;
          }
          100% {
            --rtY: 45deg;
            --rtX: 56deg;
          }
        }

        @keyframes space {
          0% {
            --space: 0px;
          }
          100% {
            --space: 180px;
          }
        }

        @keyframes move {
          0% {
            --Ctz: -515px;
          }
          100% {
            --Ctz: 615px;
          }
        }

        @keyframes fade {
          0%,
          75%,
          100% {
            opacity: 0;
          }
          0%,
          85%,
          100% {
            --scale: 0.2;
          }
          35%,
          60% {
            --scale: 1.2;
          }
          30%,
          75% {
            opacity: 1;
          }
        }

        @media (prefers-reduced-motion: reduce) {
          .tunnel-scene,
          .tunnel-box,
          .circle-active {
            animation: none;
          }

          .tunnel-scene {
            transform: rotateY(38deg) rotateX(48deg);
          }

          .tunnel-box {
            transform: translateZ(
              calc((var(--i) - (var(--tc) + 1) / 2) * 120px)
            );
          }

          .circle-active {
            opacity: 0.32;
            transform: translateZ(0) rotate(45deg) rotateY(1deg)
              rotateX(88deg) scale(0.8);
          }
        }
      `}</style>
    </div>
  );
};

export default GlassTunnel3D;
